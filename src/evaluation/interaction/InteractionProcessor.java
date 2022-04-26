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
import static simulation.Simulation.getSelectedParticles;
import static simulation.Simulation.timeStepController;

import java.util.ArrayList;
import java.util.Iterator;

import elements.force_pair.ForceElement;
import elements.force_pair.NeighborPair;
import elements.force_pair.Spring;
import elements.groups.ParticleGroup;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;
import gui.MainWindow;
import gui.Viewport;
import gui.ViewportEvent;
import gui.ViewportEvent.MouseMode;
import simulation.SimulationContent;
import simulation.components.OneTimePerStepProcessable;

public class InteractionProcessor implements OneTimePerStepProcessable {

	public static final int DEFAULT_NEIGHBOR_SEARCH_PERIOD = 25;
	private ParticleGroup particles;
	private SpringGroup springs;
	private ArrayList<ForceElement> forceElements = new ArrayList<ForceElement>();
	private boolean useExternalForces = false, usePPCollisions = true, 
			recalculateNeighborsNeeded = true, useFriction = true;
	public boolean useFastSpringProjection = true, useBoundaries = true;
	private double externalAccelerationX = 0;
	private double externalAccelerationY = -g;
	private double spaceFrictionCoefficient = 0.2;
	private double timeStepReserveRatio;
	private double dF, maxSpringForce, maxPairForce, maxParticleSquaredVelocity, mouseForceCoef;
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
		if (usePPCollisions) {
			for (int i = 0; i < particles.size() - 1; i++) {
				for (int j = i + 1; j < particles.size(); j++) {
					maxSqDist = 1.1 * (particles.get(i).getRadius() + particles.get(j).getRadius());
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
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE
				&& !getSelectedParticles().isEmpty()) {
			Particle p = getSelectedParticle(0);
			if (p != null) {
				double x = p.getX();
				double y = p.getY();
				double newx = x - (x - Viewport.fromScreenX(ViewportEvent.x1 + ViewportEvent.dx)) / 1000;
				double newy = y - (y - Viewport.fromScreenY(ViewportEvent.y1 + ViewportEvent.dy)) / 1000;
				p.setX(newx);
				p.setY(newy);
				p.setVelocity(0, 0);
			}
		}
	}

	private void accelerateSelectedParticle() {
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_ACCELERATION
				&& getSelectedParticles().size() > 0) {
			Particle p = getSelectedParticle(0);
			if (p != null) {
				mouseForceCoef = 1 * getSelectedParticle(0).getMass();
				p.addFx(-mouseForceCoef * (Viewport.toScreenX(p.getX()) - ViewportEvent.x1));
				p.addFy(mouseForceCoef * (Viewport.toScreenY(p.getY()) - ViewportEvent.y1));
			}
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

	public void movekBackParticles() {
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
		MainWindow.println(String.format("Сутыкненні па Герцу, beta = %.3e", beta));
	}

	public void setBeta(double r1, double r2, double e1, double e2, double u1, double u2) {
		this.beta = 4 / 3 / (1 / e1 - u1 * u1 / e1 + 1 / e2 - u2 * u2 / e2) / sqrt(1 / r1 + 1 / r2);
		MainWindow.println(String.format("Сутыкненні па Герцу, beta = %.3e", beta));
	}

	public void setBeta(double beta) {
		this.beta = beta;
		MainWindow.println(String.format("Сутыкненні па Герцу, beta = %.3e", beta));
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
		MainWindow.println("Знешнія сілы: " + useExternalForces);
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
		MainWindow.println("Сутыкненні часціца-часціца: " + this.usePPCollisions);
	}

	public boolean isUseSPCollisionsNeeded() {
		boolean b = false;
		System.out.println("Springs number: " + springs.size());
		for (Spring sprg : springs) {
			if (sprg.isCanCollide())
			{
				b = true;
				break;
			}			
		}
		MainWindow.println("Апрацоўка сутыкненняў часціца-пружына патрэбная: " + b);
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
		MainWindow.println(
				String.format("Макс. адлегласць для разліку ўзаемадзеянняў: %.1e м", maxPairInteractionDistance));
	}

}
