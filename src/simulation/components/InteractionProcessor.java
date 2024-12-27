package simulation.components;

import static calculation.Functions.defineSquaredDistance;
import static calculation.constants.PhysicalConstants.g;
import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import calculation.Functions;
import calculation.TrajectoryIntegrator;
import calculation.Vector;
import calculation.pairforce.Collision;
import calculation.pairforce.PairForce;
import calculation.pairforce.PairForceFactory;
import elements.group.ParticleGroup;
import elements.group.SpringGroup;
import elements.line.NeighborPair;
import elements.line.Pair;
import elements.line.Spring;
import elements.point.Particle;
import elements.point.PointMass;
import gui.ConsoleWindow;
import gui.images.FieldType;
import gui.lang.GUIStrings;
import gui.viewport.CoordinateConverter;
import simulation.ExternalForce;
import simulation.Simulation;
import simulation.SimulationContent;

public class InteractionProcessor implements SimulationComponent {

	public static final int NEIGHBORS_SEARCH_INITIAL_PERIOD = 25;
	public static final int NEIGHBORS_SEARCHES_AUTOADJUST_SAFETY = 50;
	public static final int PARTICLE_BY_MOUSE_MOVING_SMOOTH = 500;
	public static final int PARTICLE_ACCELERATION_BY_MOUSE = 2;

	private ArrayList<Pair> pairs = new ArrayList<Pair>();

	private InteractionType interactionType;
	private PairForce pairForce;
	private PairForce collisionForce;
	private ParticleGroup particles;
	private SpringGroup springs;
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
	private double pairInteractionMinDistance;
	private double pairInteractionMaxDistance;
	private double neighborRangeExtra = 1.1;
	private double neighborRange = pairInteractionMaxDistance * neighborRangeExtra;
	private long pairInteractionsNumber = 0;
	private int neighborSearchCurrentStep;
	private int neighborSearchSkipSteps;
	private int neighborSearchesNumber;

	public InteractionProcessor(SimulationContent content) {
		new Functions();
		new TrajectoryIntegrator();
		setInteractionType(InteractionType.COULOMB);
		collisionForce = new Collision();
		externalForce = new ExternalForce(0, -g);
		particles = content.getParticles();
		springs = content.getSprings();
		reset();
	}

	@Override
	public void process() {
		if (recalculateNeighborsNeeded) {
			recalculateNeighborsList();
		}
		calculateForces();
		accelerateSelectedParticle();
		calculateLocations();
		moveSelectedParticle();
		if (neighborSearchCurrentStep >= neighborSearchSkipSteps) {
			recalculateNeighborsNeeded();
		}
		adjustNeighborsSearchPeriod();
		neighborSearchCurrentStep++;
	}

	private void calculateForces() {
		NeighborPair.maxPairForceCandidate = 0;
		Spring.maxSpringForceCandidate = 0;
		double minReserve = Double.MAX_VALUE;
		Pair pair;
		Iterator<Pair> it = pairs.iterator();
		while (it.hasNext()) {
			pair = it.next();
			pair.doForce();
			if (pair.getSafetyReserve() < minReserve)
				minReserve = pair.getSafetyReserve();
		}
		NeighborPair.maxPairForce = Math.abs(NeighborPair.maxPairForceCandidate);
		Spring.maxSpringForce = Math.abs(Spring.maxSpringForceCandidate);
		timeStepReserveRatio = minReserve;
	}

	private void calculateLocations() {
		PointMass.maxSquaredVelocityCandidate = 0;
		PointMass pm;
		Iterator<Particle> it = particles.iterator();
		while (it.hasNext()) {
			pm = it.next();
			pm.doMovement();
			pm.clearForce();
		}
		PointMass.maxVelocity = Math.sqrt(PointMass.maxSquaredVelocityCandidate);
	}

	public Vector calculateField(double x, double y, FieldType fieldType) {
		Particle testParticle;
		pairForce = PairForceFactory.getCentralForce(getInteractionType());
		Vector field = new Vector();
		double distance;
		double increment = 0;
		int pNumber = 0;
		while (Simulation.getInstance().content().particle(pNumber) != null) {
			testParticle = Simulation.getInstance().content().particle(pNumber);
			distance = Functions.defineDistance(testParticle, x, y);
			if (distance >= 0.9 * testParticle.getRadius()) {
				if (fieldType == FieldType.POTENTIAL)
					increment = pairForce.calculatePotential(testParticle, distance);
				else if (fieldType == FieldType.STRENGTH)
					increment = pairForce.calculateStrength(testParticle, distance);
				else if (fieldType == FieldType.SPL)
					increment = Math.sin(2 * Math.PI * 100 * distance / 343) / distance;
				if (fieldType != FieldType.SPL) {
					field.addToX(increment * (x - testParticle.getX()) / distance);
					field.addToY(increment * (y - testParticle.getY()) / distance);
				} else {
					field.addToX(increment);
				}
			}
			pNumber++;
		}
		return field;
	}

	public Vector calculateField(int x, int y, FieldType mapType) {
		return calculateField(CoordinateConverter.fromScreenX(x), CoordinateConverter.fromScreenY(y), mapType);
	}

	private void adjustNeighborsSearchPeriod() {
		if (usePPCollisions || useInterparticleForces) {
			double t1 = getNeighborRangeExtra() / PointMass.maxVelocity;
			neighborSearchSkipSteps = (int) Math
					.round(t1 / Simulation.getInstance().timeStepController.getTimeStepSize()
							/ NEIGHBORS_SEARCHES_AUTOADJUST_SAFETY);
		}
	}

	private void recalculateNeighborsList() {
		pairs.clear();
		double squaredDistance;
		double maxPairSquaredDistance;
		if (usePPCollisions || useInterparticleForces) {
			for (int i = 0; i < particles.size() - 1; i++) {
				for (int j = i + 1; j < particles.size(); j++) {
					maxPairSquaredDistance = useInterparticleForces ? pairInteractionMaxDistance
							: neighborRangeExtra * (particles.get(i).getRadius() + particles.get(j).getRadius());
					maxPairSquaredDistance *= maxPairSquaredDistance;
					squaredDistance = defineSquaredDistance(i, j);
					if (squaredDistance <= maxPairSquaredDistance)
						pairs.add(new NeighborPair(i, j, sqrt(squaredDistance)));
				}
			}
			neighborSearchesNumber++;
		}
		pairs.addAll(springs);
		pairInteractionsNumber = pairs.size();
		recalculateNeighborsNeeded = false;
		neighborSearchCurrentStep = 0;
	}

	private void moveSelectedParticle() {
		Particle p = Simulation.getInstance().content().getSelectedParticle(0);
		if (p != null && isMoveToMouse) {
			p.setVelocity(0, 0);
			double newx = p.getX() + (particleTargetXY.getX() - p.getX()) / PARTICLE_BY_MOUSE_MOVING_SMOOTH;
			double newy = p.getY() + (particleTargetXY.getY() - p.getY()) / PARTICLE_BY_MOUSE_MOVING_SMOOTH;
			p.setX(newx);
			p.setY(newy);
		}
	}

	private void accelerateSelectedParticle() {
		Particle p = Simulation.getInstance().content().getSelectedParticle(0);
		if (p != null && isAccelerateByMouse) {
			Point2D.Double particleMouseDifferenceXY = new Point2D.Double(
					particleTargetXY.getX() - Simulation.getInstance().content().getSelectedParticle(0).getX(),
					particleTargetXY.getY() - Simulation.getInstance().content().getSelectedParticle(0).getY());
			double force = PARTICLE_ACCELERATION_BY_MOUSE
					* Simulation.getInstance().content().getSelectedParticle(0).getMass()
					* Math.pow(particleMouseDifferenceXY.distance(0, 0), 3);
			double angle = Math.atan2(particleMouseDifferenceXY.getY(), particleMouseDifferenceXY.getX());
			p.addFx(force * Math.cos(angle));
			p.addFy(force * Math.sin(angle));
		}

	}

	public void moveBackParticles() {
		Particle p;
		for (int i = 0; i < Simulation.getInstance().content().getParticlesCount(); i++) {
			p = Simulation.getInstance().content().particle(i);
			p.setXnoHistory(p.getLastX());
			p.setYnoHistory(p.getLastY());
			p.getVelocityVector().setX(p.getLastVx());
			p.getVelocityVector().setY(p.getLastVy());
			p.clearForcesAndHistory();
		}
	}

	@Override
	public void setSkipStepsNumber(int skip) {
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

	public long getPairInteractionCount() {
		return pairInteractionsNumber;
	}

	public void reset() {
		neighborSearchCurrentStep = 0;
		neighborSearchesNumber = 0;
		neighborSearchSkipSteps = NEIGHBORS_SEARCH_INITIAL_PERIOD;
		recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.INTERACTION_PROCESSOR_RESTARTED);
	}

	public void setInteractionType(InteractionType interactionType) {
		pairForce = PairForceFactory.getCentralForce(interactionType);
		pairInteractionMaxDistance = pairForce.distanceLimit();
		neighborRange = pairInteractionMaxDistance * 1.1;
		this.interactionType = interactionType;
		message();
	}

	public InteractionType getInteractionType() {
		return interactionType;
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

	public double getTimeStepReserveRatio() {
		return timeStepReserveRatio;
	}

	public PairForce pairForceType() {
		return pairForce;
	}

	public PairForce collisionForceType() {
		return collisionForce;
	}

	public void message() {
		ConsoleWindow.println(
				String.format(GUIStrings.MAX_INTERACTION_DEFINING_DISTANCE + ": %.1e m", pairInteractionMaxDistance));
	}

}
