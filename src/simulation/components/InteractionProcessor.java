package simulation.components;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.dj;
import static constants.PhysicalConstants.g;
import static constants.PhysicalConstants.m;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static simulation.math.Functions.defineSquaredDistance;
import static simulation.math.Functions.sqr;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import elements.Movable;
import elements.group.ParticleGroup;
import elements.group.SpringGroup;
import elements.line.NeighborPair;
import elements.line.Spring;
import elements.point.Particle;
import elements.point.PointMass;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;
import simulation.ExternalForce;
import simulation.Simulation;
import simulation.SimulationContent;
import simulation.math.Functions;
import simulation.math.LennardJonesFunction;
import simulation.math.PairForce;
import simulation.math.TabulatedFunction;
import simulation.math.TrajectoryIntegrator;

public class InteractionProcessor implements SimulationComponent {

	public static final int NEIGHBORS_SEARCH_INITIAL_PERIOD = 25;
	public static final int NEIGHBORS_SEARCHES_AUTOADJUST_SAFETY = 10;
	public static final int PARTICLE_BY_MOUSE_MOVING_SMOOTH = 500;
	public static final int PARTICLE_ACCELERATION_BY_MOUSE = 2;

	private ArrayList<Movable> movables = new ArrayList<Movable>();

	private static InteractionType interactionType = InteractionType.COULOMB;
	private static PairForce pairForce;
	private static TabulatedFunction forceTable;
	private static ParticleGroup particles;
	private static SpringGroup springs;
	private ExternalForce externalForce;
	private Point2D.Double particleTargetXY = new Point2D.Double(0, 0);
	
	private boolean useExternalForces = false;
	private boolean usePPCollisions = true;
	private boolean recalculateNeighborsNeeded = true;
	private boolean useFriction = true;
	private boolean useInterparticleForces = true;
	public boolean useFastSpringProjection = true;
	private boolean isMoveToMouse;
	private boolean isAccelerateByMouse;
	private double spaceFrictionCoefficient = 0.1;
	private double timeStepReserveRatio;
	private double df, maxSpringTension, maxPairForce;
	private double pairInteractionMinDistance = 1E-9;
	private double pairInteractionMaxDistance = 1.5 * m;
	private double neighborRange = pairInteractionMaxDistance * 1.1;
	private long pairInteractionsNumber = 0;
	private int skipSteps = 0;
	private int currentStep;
	private int neighborSearchCurrentStep;
	private int neighborSearchSkipSteps;
	private int neighborSearchesNumber;

	public InteractionProcessor(SimulationContent content) {
		new Functions();
		new TrajectoryIntegrator();
		pairForce = new PairForce();
		externalForce = new ExternalForce(0, -g);
		particles = content.getParticles();
		springs = content.getSprings();
		reset();
		setInteractionType(InteractionType.COULOMB, forceTable);
	}

	@Override
	public void process() {
		if (recalculateNeighborsNeeded)
			recalculateNeighborsList();
		if (currentStep >= skipSteps) {
			currentStep = 0;
			calculateForcesAndLocations();
			accelerateSelectedParticle();
		}
		moveSelectedParticle();
		if (neighborSearchCurrentStep > neighborSearchSkipSteps) {
			recalculateNeighborsNeeded();
		}
		adjustNeighborsSearchPeriod();
		currentStep++;
		neighborSearchCurrentStep++;
	}

	private void calculateForcesAndLocations() {
		maxSpringTension = 0;
		maxPairForce = 0;
		PointMass.maxSquaredVelocityCandidate = 0;
		double currentForce, maxForce = 0, reserve = Double.MAX_VALUE;
		Movable movable;
		Iterator<Movable> it = movables.iterator();
		while (it.hasNext()) {
			movable = it.next();
			movable.doMovement();
			currentForce = abs(movable.getForceValue());
			if (currentForce > maxForce)
				maxForce = currentForce;
			if (movable.getSafetyReserve() < reserve)
				reserve = movable.getSafetyReserve();
			if (currentStep == 0)
				movable.clearForce();
		}
		PointMass.maxVelocity = Math.sqrt(PointMass.maxSquaredVelocityCandidate);
		maxPairForce = maxForce;
		timeStepReserveRatio = reserve;
	}

	private void adjustNeighborsSearchPeriod() {
		if (usePPCollisions || useInterparticleForces) {
			double t1 = getNeighborRangeExtra() / PointMass.maxVelocity;
			neighborSearchSkipSteps = (int) Math
					.round(t1 / Simulation.getInstance().timeStepController.getTimeStepSize() / NEIGHBORS_SEARCHES_AUTOADJUST_SAFETY);
		}
	}

	private void recalculateNeighborsList() {
		movables.clear();
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
						movables.add(new NeighborPair(i, j, sqrt(sqDist)));
				}
			}
			neighborSearchesNumber++;
		}
		movables.addAll(springs);
		pairInteractionsNumber = movables.size();
		movables.addAll(particles);
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
			Functions.addForce(i, j, df, distance);
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

	private void recoilByHertz(Particle p1, Particle p2, double distance) {
		df = pairForce.defineHertzForce(p1.getRadius() + p2.getRadius() - distance);
		Functions.addForce(p1, p2, df, distance);
	}

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
		return neighborRange - pairInteractionMaxDistance;
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
		return pairInteractionMinDistance;
	}

	public void setMinPairInteractionDistance(double minPairInteractionDistance) {
		this.pairInteractionMinDistance = minPairInteractionDistance;
	}

	public double getMaxPairInteractionDistance() {
		return pairInteractionMaxDistance;
	}

	public void setMaxPairInteractionDistance(double maxPairInteractionDistance) {
		this.pairInteractionMaxDistance = maxPairInteractionDistance;
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

	public long getPairInteractionCount() {
		return pairInteractionsNumber;
	}

	public void reset() {
		currentStep = 0;
		neighborSearchCurrentStep = 0;
		neighborSearchesNumber = 0;
		neighborSearchSkipSteps = NEIGHBORS_SEARCH_INITIAL_PERIOD;
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
				forceTable = new LennardJonesFunction(pairInteractionMinDistance, 150 * cm, 0.1 * cm);
				forceTable.setParam1(20 * cm);
				forceTable.setParam2(40 * dj);
				forceTable.calculateTable();
			}
			pairInteractionMaxDistance = forceTable.getParam1() * 2.5;
		} else if (interactionType == InteractionType.COULOMB) {
			pairInteractionMaxDistance = 15 * m;
		} else if (interactionType == InteractionType.GRAVITATION
				|| interactionType == InteractionType.COULOMB_AND_GRAVITATION) {
			pairInteractionMaxDistance = Double.MAX_VALUE;
		}
		InteractionProcessor.interactionType = interactionType;
		neighborRange = pairInteractionMaxDistance * 1.1;
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
		return neighborSearchesNumber;
	}

	public int getNeighborSearchsSkipStepsNumber() {
		return neighborSearchSkipSteps;
	}

	public void setMaxSpringForceCandidate(double force) {
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
	
	public boolean isUseInterparticleForces() {
		return useInterparticleForces;
	}

	public void setUseInterparticleForces(boolean useInterparticleForces) {
		this.useInterparticleForces = useInterparticleForces;
		recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.INTERPARTICLE_FORCES + ": " + this.useInterparticleForces);
	}

	public double getMaxPairForce() {
		return maxPairForce;
	}

	public double getTimeStepReserveRatio() {
		return timeStepReserveRatio;
	}

	public void message() {
		ConsoleWindow.println(
				String.format(GUIStrings.MAX_INTERACTION_DEFINING_DISTANCE + ": %.1e m", pairInteractionMaxDistance));
	}

}
