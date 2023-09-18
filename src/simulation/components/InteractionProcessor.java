package simulation.components;

import static constants.PhysicalConstants.ang;
import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.dj;
import static constants.PhysicalConstants.g;
import static constants.PhysicalConstants.m;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static simulation.math.Functions.defineSquaredDistance;
import static simulation.math.Functions.fastSqrt;
import static simulation.math.Functions.sqr;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import elements.group.ParticleGroup;
import elements.group.SpringGroup;
import elements.line.NeighborPair;
import elements.line.Pair;
import elements.line.Spring;
import elements.point.Particle;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;
import simulation.Boundaries;
import simulation.ExternalForce;
import simulation.Simulation;
import simulation.SimulationContent;
import simulation.math.Functions;
import simulation.math.LennardJonesFunction;
import simulation.math.PairForce;
import simulation.math.TabulatedFunction;
import simulation.math.TrajectoryIntegrator;

public class InteractionProcessor implements SimulationComponent {

	public static final int DEFAULT_NEIGHBOR_SEARCH_PERIOD = 25;
	private static final int PARTICLE_BY_MOUSE_MOVING_SMOOTH = 500;
	private static final int PARTICLE_ACCELERATION_BY_MOUSE = 2;
	private static InteractionType interactionType = InteractionType.COULOMB;
	private static TrajectoryIntegrator integrator;
	private static PairForce pairForce;
	private static TabulatedFunction forceTable;
	private static ParticleGroup particles;
	private static SpringGroup springs;
	
	private ArrayList<Pair> forcePairs = new ArrayList<Pair>();
	private boolean useExternalForces = false, usePPCollisions = true, recalculateNeighborsNeeded = true,
			useFriction = true, useInterparticleForces = true;
	public boolean useFastSpringProjection = true, useBoundaries = true;
	private boolean isMoveToMouse;
	private boolean isAccelerateByMouse;
	private ExternalForce externalForce;
	private Point2D.Double particleTargetXY = new Point2D.Double(0, 0);
	private double spaceFrictionCoefficient = 0.1;
	private double timeStepReserveRatio;
	private double df, maxSpringTension, maxPairForce, maxParticleSquaredVelocity;
	private double minPairInteractionDistance = 1 * ang, maxPairInteractionDistance = 1.5 * m,
			neighborRange = maxPairInteractionDistance * 1.1;
	private long pairInteractionsNumber = 0;
	private int skipSteps = 0, currentStep, neighborSearchCurrentStep, neighborSearchSkipSteps, neighborSearchNumber;

	public InteractionProcessor(SimulationContent content) {
		new Functions();
		integrator = new TrajectoryIntegrator();
		pairForce = new PairForce();
		externalForce = new ExternalForce(0, -g);
		particles = content.getParticles();
		springs = content.getSprings();
		if (interactionType==InteractionType.LENNARDJONES) {
			forceTable = new LennardJonesFunction(minPairInteractionDistance, 150 * cm, 0.1 * cm);
			forceTable.setParam1(20 * cm);
			forceTable.setParam2(40 * dj);
			forceTable.calculateTable();
		}
		reset();
	}

	@Override
	public void process() {
		if (recalculateNeighborsNeeded)
			recalculateNeighborsList();
		if (currentStep >= skipSteps) {
			calculateForces();
			accelerateSelectedParticle();
			currentStep = 0;
		}
		calculateLocations();
		if (neighborSearchCurrentStep > neighborSearchSkipSteps) {
			recalculateNeighborsNeeded();
			adjustNeighborsListRefreshPeriod();
		}
		currentStep++;
		neighborSearchCurrentStep++;
	}

	private void calculateForces() {
		maxSpringTension = 0;
		maxPairForce = 0;
		double f, maxF = 0, rr = Double.MAX_VALUE;
		Pair pair;
		Iterator<Pair> it = forcePairs.iterator();
		while (it.hasNext()) {
			pair = it.next();
			pair.applyForce();
			f = abs(pair.getForce());
			if (f > maxF)
				maxF = f;
			if (pair.getReserveRatio() < rr)
				rr = pair.getReserveRatio();
		}
		pairInteractionsNumber = forcePairs.size();
		maxPairForce = maxF;
		timeStepReserveRatio = rr;
	}

	private void adjustNeighborsListRefreshPeriod() {
		if (usePPCollisions) {
			double t1 = getNeighborRangeExtra() / defineMaxParticleVelocity();
			neighborSearchSkipSteps = (int) Math
					.round(t1 / Simulation.getInstance().timeStepController.getTimeStepSize() / 100);
			if (neighborSearchSkipSteps > Simulation.getInstance().timeStepController.getStepsPerSecond() / 2)
				neighborSearchSkipSteps = Math
						.round(Simulation.getInstance().timeStepController.getStepsPerSecond() / 2);
		}
	}

	private void recalculateNeighborsList() {
		forcePairs.clear();
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
						forcePairs.add(new NeighborPair(i, j, sqrt(sqDist)));
				}
			}
			neighborSearchNumber++;
		}
		forcePairs.addAll(springs);
		recalculateNeighborsNeeded = false;
		neighborSearchCurrentStep = 0;
	}

	public double applyPairInteraction(Particle i, Particle j, double distance) {
		df = 0;
		if (useInterparticleForces) {
			if (interactionType == InteractionType.COULOMB)
				df = pairForce.defineCoulombForce(i, j, distance);
			else if (interactionType == InteractionType.LENNARDJONES)
				df = forceTable.getFromTable(distance);
			else if (interactionType == InteractionType.GRAVITATION)
				df = pairForce.defineGravitationForce(i, j, distance);
			else if (interactionType == InteractionType.COULOMB_AND_GRAVITATION)
				df = pairForce.defineCoulombForce(i, j, distance) + pairForce.defineGravitationForce(i, j, distance);
			applyForceParallelToDistance(i, j, df, distance);
		}
		if (usePPCollisions) {
			if (i.isCanCollide() && j.isCanCollide()) {
				double collisionEventSquaredDistance = sqr(i.getRadius() + j.getRadius());
				if (sqr(distance) < collisionEventSquaredDistance)
					recoilByHertz(i, j, distance);
			}
		}
		return df;
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
		df = pairForce.defineHertzForce(p1.getRadius() + p2.getRadius() - distance);
		applyForceParallelToDistance(p1, p2, df, distance);
	}

	// private void recoilByAcceleration(Particle p1, Particle p2, double
	// distance) {
	// double x = p1.getRadius() + p2.getRadius() - distance;
	// dF = 500 * g * x * (p1.getMass() + p2.getMass());
	// applyForceParallelToDistance(p1, p2, dF, distance);
	// }

	private void moveSelectedParticle() {
		Particle p = Simulation.getInstance().getContent().getSelectedParticle(0);
		if (p != null && isMoveToMouse) {
			p.setVelocity(0, 0);
			double newx = p.getX() + (particleTargetXY.getX() - p.getX()) / PARTICLE_BY_MOUSE_MOVING_SMOOTH;
			double newy = p.getY() + (particleTargetXY.getY() - p.getY()) / PARTICLE_BY_MOUSE_MOVING_SMOOTH;
			p.setX(newx);
			p.setY(newy);
		}
	}

	private void accelerateSelectedParticle() {
		Particle p = Simulation.getInstance().getContent().getSelectedParticle(0);
		if (p != null && isAccelerateByMouse) {
			Point2D.Double particleMouseDifferenceXY = new Point2D.Double(
					particleTargetXY.getX() - Simulation.getInstance().getContent().getSelectedParticle(0).getX(),
					particleTargetXY.getY() - Simulation.getInstance().getContent().getSelectedParticle(0).getY());
			double force = PARTICLE_ACCELERATION_BY_MOUSE
					* Simulation.getInstance().getContent().getSelectedParticle(0).getMass()
					* Math.pow(particleMouseDifferenceXY.distance(0, 0), 3);
			double angle = Math.atan2(particleMouseDifferenceXY.getY(), particleMouseDifferenceXY.getX());
			p.addFx(force * Math.cos(angle));
			p.addFy(force * Math.sin(angle));
		}

	}

	private void calculateLocations() {
		double maxVel = 0, vel;
		Particle p;
		Iterator<Particle> it = particles.iterator();
		while (it.hasNext()) {
			p = it.next();
			if (useExternalForces)
				externalForce.apply(p);
			integrator.calculateNextVelocity(p, Simulation.getInstance().timeStepController.getTimeStepSize(),
					useFriction);
			vel = p.getVelocityVector().normSquared();
			if (vel > maxVel)
				maxVel = vel;
			p.calculateNextLocation(Simulation.getInstance().timeStepController.getTimeStepSize());
			if (currentStep == 0)
				p.clearForce();
			if (useBoundaries) {
				Boundaries b = Simulation.getInstance().getContent().getBoundaries();
				b.applyBoundaryConditions(p);
			}
		}
		moveSelectedParticle();
		maxParticleSquaredVelocity = maxVel;
	}

	public void moveBackParticles() {
		Particle p;
		for (int i = 0; i < Simulation.getInstance().getContent().getParticlesCount(); i++) {
			p = Simulation.getInstance().getContent().getParticle(i);
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

	public double getMaxSpringTension() {
		return maxSpringTension;
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
	
	public void setInteractionType(InteractionType interactionType, TabulatedFunction potentialTable) {
		if (interactionType == InteractionType.LENNARDJONES) {
			if (potentialTable != null) {
				forceTable = potentialTable;
				System.out.println("Potential table is confirmed");
			} else if (forceTable == null) {
				System.out.println("Potential table is null!");
				forceTable = new LennardJonesFunction(minPairInteractionDistance, 150 * cm, 0.1 * cm);
				forceTable.setParam1(20 * cm);
				forceTable.setParam2(0.004 * dj);
				forceTable.calculateTable();
			}
			maxPairInteractionDistance = forceTable.getParam1() * 2.5;
		} else if (interactionType == InteractionType.COULOMB) {
			maxPairInteractionDistance = 15 * m;
		} else if (interactionType == InteractionType.GRAVITATION
				|| interactionType == InteractionType.COULOMB_AND_GRAVITATION) {
			maxPairInteractionDistance = Double.MAX_VALUE;
		}
		this.interactionType = interactionType;
		neighborRange = maxPairInteractionDistance * 1.1;
		message();
	}

	public ExternalForce getExternalForce() {
		return externalForce;
	}

	public void setExternalForce(ExternalForce ef) {
		if (ef != null)
			this.externalForce = ef;
		else
			throw new RuntimeException("Interaction processors external force set to null!");
	}

	public int getNeighborSearchsNumber() {
		return neighborSearchNumber;
	}

	public int getNeighborSearchsSkipStepsNumber() {
		return neighborSearchSkipSteps;
	}

	public void tryToSetMaxSpringForce(double force) {
		if (force > maxSpringTension)
			maxSpringTension = force;
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
