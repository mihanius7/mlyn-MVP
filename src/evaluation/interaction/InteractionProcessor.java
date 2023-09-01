package evaluation.interaction;

import static constants.PhysicalConstants.G;
import static constants.PhysicalConstants.ang;
import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.g;
import static constants.PhysicalConstants.k;
import static constants.PhysicalConstants.m;
import static evaluation.MyMath.defineSquaredDistance;
import static evaluation.MyMath.fastSqrt;
import static evaluation.MyMath.sqr;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static simulation.Simulation.getParticle;
import static simulation.Simulation.getParticlesCount;
import static simulation.Simulation.getSelectedParticle;
import static simulation.Simulation.timeStepController;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import elements.force_pair.ForceElement;
import elements.force_pair.NeighborPair;
import elements.force_pair.Spring;
import elements.groups.ParticleGroup;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;
import gui.ConsoleWindow;
import gui.CoordinateConverter;
import gui.lang.GUIStrings;
import simulation.SimulationContent;
import simulation.components.OneTimePerStepProcessable;

public class InteractionProcessor implements OneTimePerStepProcessable {

	public static final int DEFAULT_NEIGHBOR_SEARCH_PERIOD = 25;
	private static final int PARTICLE_BY_MOUSE_MOVING_SMOOTH = 500;
	private static final int PARTICLE_ACCELERATION_BY_MOUSE = 2;
	private InteractionType interactionType = InteractionType.COULOMB;
	private TabulatedFunction forceTable;
	private ParticleGroup particles;
	private SpringGroup springs;
	private ArrayList<ForceElement> forceElements = new ArrayList<ForceElement>();
	private boolean useExternalForces = false, usePPCollisions = true, recalculateNeighborsNeeded = true,
			useFriction = true, useInterparticleForces = true;
	public boolean useFastSpringProjection = true, useBoundaries = true;
	private boolean isMoveToMouse;
	private boolean isAccelerateByMouse;
	private double externalAccelerationX = 0;
	private double externalAccelerationY = -g;
	private Point2D.Double particleTargetXY = new Point2D.Double(0, 0);
	private double spaceFrictionCoefficient = 0.2;
	private double timeStepReserveRatio;
	private double dF, maxSpringForce, maxPairForce, maxParticleSquaredVelocity;
	private double minPairInteractionDistance = 1 * ang, maxPairInteractionDistance = 1.5 * m,
			neighborRange = maxPairInteractionDistance * 1.1;
	private long pairInteractionsNumber = 0;
	private int skipSteps = 0, currentStep, neighborSearchCurrentStep, neighborSearchSkipSteps, neighborSearchNumber;
	private double beta = (2 * 1E6 * sqrt(5 * cm * 5 * cm / (5 * cm + 5 * cm))) / (3 * (1 - 0.28 * 0.28));

	public InteractionProcessor(SimulationContent content) {
		particles = content.getParticles();
		springs = content.getSprings();
		reset();
	}

	@Override
	public void process() {
		if (recalculateNeighborsNeeded)
			recalculateNeighborsList();
		if (currentStep > skipSteps) {
			applyNeighborInteractions();
			accelerateSelectedParticle();
			currentStep = 0;
		}
		moveParticles();
		if (neighborSearchCurrentStep > neighborSearchSkipSteps) {
			recalculateNeighborsNeeded();
			adjustNeighborsListRefreshPeriod();
		}
		currentStep++;
		neighborSearchCurrentStep++;
	}

	private void applyNeighborInteractions() {
		maxSpringForce = 0;
		maxPairForce = 0;
		double f, maxF = 0, rr = Double.MAX_VALUE;
		ForceElement fe;
		Iterator<ForceElement> it = forceElements.iterator();
		while (it.hasNext()) {
			fe = it.next();
			fe.applyForce();
			f = abs(fe.getForce());
			if (f > maxF)
				maxF = f;
			if (fe.getReserveRatio() < rr)
				rr = fe.getReserveRatio();
		}
		pairInteractionsNumber = forceElements.size();
		maxPairForce = maxF;
		timeStepReserveRatio = rr;
	}

	private void adjustNeighborsListRefreshPeriod() {
		if (usePPCollisions) {
			double t1 = getNeighborRangeExtra() / defineMaxParticleVelocity();
			neighborSearchSkipSteps = (int) Math.round(t1 / timeStepController.getTimeStepSize() / 100);
			if (neighborSearchSkipSteps > timeStepController.getStepsPerSecond() / 2)
				neighborSearchSkipSteps = Math.round(timeStepController.getStepsPerSecond() / 2);
		}
	}

	private void recalculateNeighborsList() {
		forceElements.clear();
		double sqDist, maxSqDist;
		if (usePPCollisions || useInterparticleForces) {
			for (int i = 0; i < particles.size() - 1; i++) {
				for (int j = i + 1; j < particles.size(); j++) {
					maxSqDist = (useInterparticleForces)
							? neighborRange + particles.get(i).getRadius() + particles.get(j).getRadius()
							: 1.1 * (particles.get(i).getRadius() + particles.get(j).getRadius());
					maxSqDist *= maxSqDist;
					sqDist = defineSquaredDistance(i, j);
					if (sqDist <= maxSqDist)
						forceElements.add(new NeighborPair(i, j, sqrt(sqDist)));
				}
			}
			neighborSearchNumber++;
		}
		forceElements.addAll(springs);
		recalculateNeighborsNeeded = false;
		neighborSearchCurrentStep = 0;
	}

	public double applyPairInteraction(Particle i, Particle j, double distance) {
		dF = 0;
		if (useInterparticleForces) {
			if (interactionType == InteractionType.COULOMB)
				dF = defineCoulombForce(i, j, distance);
			else if (interactionType == InteractionType.TABULATED)
				dF = forceTable.getFromTable(distance);
			else if (interactionType == InteractionType.GRAVITATION)
				dF = defineGravitationForce(i, j, distance);
			else if (interactionType == InteractionType.COULOMB_AND_GRAVITATION)
				dF = defineCoulombForce(i, j, distance) + defineGravitationForce(i, j, distance);
			applyForceParallelToDistance(i, j, dF, distance);
		}
		if (usePPCollisions) {
			if (i.isCanCollide() && j.isCanCollide()) {
				double collisionEventSquaredDistance = sqr(i.getRadius() + j.getRadius());
				if (sqr(distance) < collisionEventSquaredDistance)
					recoilByHertz(i, j, distance);
			}
		}
		return dF;
	}

	public void applyForceParallelToDistance(Particle i, Particle j, double force, double distance) {
		double forceX = force * (j.getX() - i.getX()) / distance;
		double forceY = force * (j.getY() - i.getY()) / distance;
		i.addFx(-forceX);
		j.addFx(forceX);
		i.addFy(-forceY);
		j.addFy(forceY);
	}

	public void applyForcePerpendicularToDistance(Particle i, Particle j, double force, double distance) {
		double forceX = force * (j.getY() - i.getY()) / distance;
		double forceY = force * (j.getX() - i.getX()) / distance;
		i.addFx(forceX);
		j.addFx(-forceX);
		i.addFy(-forceY);
		j.addFy(forceY);
	}

	public void applyForceByAngle(Particle i, Particle j, double force, double angle) {
		double forceX = force * Math.cos(angle);
		double forceY = force * Math.sin(angle);
		i.addFx(forceX);
		j.addFx(-forceX);
		i.addFy(-forceY);
		j.addFy(forceY);
	}

	private void recoilByHertz(Particle p1, Particle p2, double distance) {
		dF = HertzForce(p1.getRadius() + p2.getRadius() - distance);
		applyForceParallelToDistance(p1, p2, dF, distance);
	}

	private double HertzForce(double x) {
		return beta * Math.pow(x, 1.5);
	}

	// private void recoilByAcceleration(Particle p1, Particle p2, double
	// distance) {
	// double x = p1.getRadius() + p2.getRadius() - distance;
	// dF = 500 * g * x * (p1.getMass() + p2.getMass());
	// applyForceParallelToDistance(p1, p2, dF, distance);
	// }

	private void moveSelectedParticle() {
		Particle p = getSelectedParticle(0);
		if (p != null && isMoveToMouse) {
			p.setVelocity(0, 0);
			double newx = p.getX() + (particleTargetXY.getX() - p.getX()) / PARTICLE_BY_MOUSE_MOVING_SMOOTH;
			double newy = p.getY() + (particleTargetXY.getY() - p.getY()) / PARTICLE_BY_MOUSE_MOVING_SMOOTH;
			p.setX(newx);
			p.setY(newy);
		}
	}

	private void accelerateSelectedParticle() {
		Particle p = getSelectedParticle(0);
		if (p != null && isAccelerateByMouse) {
			Point2D.Double particleMouseDifferenceXY = new Point2D.Double(
					particleTargetXY.getX() - getSelectedParticle(0).getX(),
					particleTargetXY.getY() - getSelectedParticle(0).getY());
			double force = PARTICLE_ACCELERATION_BY_MOUSE * getSelectedParticle(0).getMass()
					* Math.pow(particleMouseDifferenceXY.distance(0, 0), 3);
			double angle = Math.atan2(particleMouseDifferenceXY.getY(), particleMouseDifferenceXY.getX());
			p.addFx(force * Math.cos(angle));
			p.addFy(force * Math.sin(angle));
		}

	}

	private void moveParticles() {
		double dt = timeStepController.getTimeStepSize();
		double maxVel = 0, vel;
		Particle p;
		Iterator<Particle> it = particles.iterator();
		while (it.hasNext()) {
			p = it.next();
			p.applyNewVelocity(dt, useFriction);
			vel = p.getVelocityVector().normSquared();
			if (vel > maxVel)
				maxVel = vel;
			p.move(dt);
			if (currentStep == 0)
				p.clearForce();
			if (useBoundaries)
				p.applyBoundaryConditions();
		}
		moveSelectedParticle();
		maxParticleSquaredVelocity = maxVel;
	}

	public void moveBackParticles() {
		Particle p;
		for (int i = 0; i < getParticlesCount(); i++) {
			p = getParticle(i);
			p.setXnoHistory(p.getLastX());
			p.setYnoHistory(p.getLastY());
			p.getVelocityVector().setX(p.getLastVx());
			p.getVelocityVector().setY(p.getLastVy());
			p.clearForcesAndHistory();
		}
	}

	@Override
	public void setSkipStepsNumber(int skip) {
		skipSteps = skip;
	}

	public double getNeighborRangeExtra() {
		return neighborRange - maxPairInteractionDistance;
	}

	public void recalculateNeighborsNeeded() {
		recalculateNeighborsNeeded = true;
	}

	public double getExternalAccelerationX() {
		return externalAccelerationX;
	}

	public void setExternalAccelerationX(double ax) {
		externalAccelerationX = ax;
	}

	public double getExternalAccelerationY() {
		return externalAccelerationY;
	}

	public void setExternalAccelerationY(double ay) {
		externalAccelerationY = ay;
	}

	public double getAirFrictionCoefficient() {
		return spaceFrictionCoefficient;
	}

	public void setSpaceFrictionCoefficient(double spaceFrictionCoefficient) {
		this.spaceFrictionCoefficient = spaceFrictionCoefficient;
	}

	public double getMinPairInteractionDistance() {
		return minPairInteractionDistance;
	}

	public void setMinPairInteractionDistance(double minPairInteractionDistance) {
		this.minPairInteractionDistance = minPairInteractionDistance;
	}

	public void setBeta(double r1, double r2, double e, double u) {
		this.beta = (2 * e * sqrt(r1 * r2 / (r1 + r2))) / (3 * (1 - u * u));
		ConsoleWindow.println(String.format(GUIStrings.RECOIL_BY_HERTZ + ", beta = %.3e", beta));
	}

	public void setBeta(double r1, double r2, double e1, double e2, double u1, double u2) {
		this.beta = 4 / 3 / (1 / e1 - u1 * u1 / e1 + 1 / e2 - u2 * u2 / e2) / sqrt(1 / r1 + 1 / r2);
		ConsoleWindow.println(String.format(GUIStrings.RECOIL_BY_HERTZ + ", beta = %.3e", beta));
	}

	public void setBeta(double beta) {
		this.beta = beta;
		ConsoleWindow.println(String.format(GUIStrings.RECOIL_BY_HERTZ + ", beta = %.3e", beta));
	}

	public double getMaxPairInteractionDistance() {
		return maxPairInteractionDistance;
	}

	public void setMaxPairInteractionDistance(double maxPairInteractionDistance) {
		this.maxPairInteractionDistance = maxPairInteractionDistance;
	}

	public boolean isUseExternalForces() {
		return useExternalForces;
	}

	public void setUseExternalForces(boolean b) {
		useExternalForces = b;
		ConsoleWindow.println(GUIStrings.EXTERNAL_FORCES + ": " + useExternalForces);
	}

	public void setParticleTargetXY(Point2D.Double particleTargetXY) {
		this.particleTargetXY = particleTargetXY;
	}

	public void setMoveToMouse(boolean b) {
		this.isMoveToMouse = b;
	}
	
	public void setAccelerateByMouse(boolean b) {
		this.isAccelerateByMouse = b;
	}

	public double defineCoulombForce(Particle particle1, Particle particle2, double distance) {
		double q1 = particle1.getCharge();
		double q2 = particle2.getCharge();
		return k * q1 * q2 / sqr(distance);
	}

	public double defineCoulombForceSq(Particle particle1, Particle particle2, double squaredDistance) {
		double q1 = particle1.getCharge();
		double q2 = particle2.getCharge();
		return k * q1 * q2 / squaredDistance;
	}

	public double defineCoulombFieldStrength(Particle particle1, double squaredDistance) {
		double q1 = particle1.getCharge();
		return k * q1 / squaredDistance;
	}

	public double defineGravitationForce(Particle particle1, Particle particle2, double distance) {
		double m1 = particle1.getMass();
		double m2 = particle2.getMass();
		return -G * m1 * m2 / sqr(distance);
	}

	public double defineGravitationFieldStrength(Particle particle1, double squaredDistance) {
		double m1 = particle1.getMass();
		return -G * m1 / squaredDistance;
	}

	public double getMaxSpringForce() {
		return maxSpringForce;
	}

	public double defineMaxParticleVelocity() {
		return fastSqrt(maxParticleSquaredVelocity);
	}

	public double getMaxParticleSquaredVelocity() {
		return maxParticleSquaredVelocity;
	}

	public long getPairInteractionCount() {
		return pairInteractionsNumber;
	}

	public void reset() {
		currentStep = 0;
		neighborSearchCurrentStep = 0;
		neighborSearchNumber = 0;
		neighborSearchSkipSteps = DEFAULT_NEIGHBOR_SEARCH_PERIOD;
		recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.INTERACTION_PROCESSOR_RESTARTED);
	}

	public int getNeighborSearchsNumber() {
		return neighborSearchNumber;
	}

	public int getNeighborSearchsSkipStepsNumber() {
		return neighborSearchSkipSteps;
	}

	public void tryToSetMaxSpringForce(double force) {
		if (force > maxSpringForce)
			maxSpringForce = force;
	}

	public boolean isUsePPCollisions() {
		return usePPCollisions;
	}

	public void setUsePPCollisions(boolean b) {
		this.usePPCollisions = b;
		recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.COLLISIONS_PP + ": " + this.usePPCollisions);
	}

	public boolean isUseSPCollisionsNeeded() {
		boolean b = false;
		System.out.println(GUIStrings.SPRINGS_NUMBER + ": " + springs.size());
		for (Spring sprg : springs) {
			if (sprg.isCanCollide()) {
				b = true;
				break;
			}
		}
		ConsoleWindow.println(GUIStrings.COLLISIONS_PS_NEEDED + ": " + b);
		return b;
	}

	public boolean isUseFriction() {
		return useFriction;
	}

	public void setUseFriction(boolean useFriction) {
		this.useFriction = useFriction;
	}

	public double getMaxPairForce() {
		return maxPairForce;
	}

	public double getTimeStepReserveRatio() {
		return timeStepReserveRatio;
	}

	public void message() {
		ConsoleWindow.println(
				String.format(GUIStrings.MAX_INTERACTION_DEFINING_DISTANCE + ": %.1e m", maxPairInteractionDistance));
	}

}
