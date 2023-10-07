package elements.line;

import static java.lang.Math.min;
import static simulation.Simulation.getInstance;
import static simulation.math.Functions.defineDistance;

import elements.point.Particle;
import simulation.math.Functions;

public class Pair {

	protected final Particle p1, p2;
	protected double force;
	protected double forceSmoothed;
	protected double distance; 
	protected double lastDistance;
	protected double criticalShift; 
	protected double angle;
	protected double timeStepReserve;

	public Pair(Particle i, Particle j) {
		p1 = getInstance().content().particleWithLesserIndex(i, j);
		p2 = getInstance().content().particleWithLargerIndex(i, j);
		setupCriticalShift();
	}

	public Pair(int i, int j) {
		this(getInstance().content().particleWithLesserIndex(
				getInstance().content().particle(i),
				getInstance().content().particle(j)),
				getInstance().content().particleWithLargerIndex(
						getInstance().content().particle(i),
						getInstance().content().particle(j)));
	}

	private void setupCriticalShift() {
		criticalShift = 0.25 * min(p1.getRadius(), p2.getRadius());
	}

	private void calculateTimeStepReserve() {
		timeStepReserve = criticalShift / Math.abs(lastDistance - distance);
	}

	public double getForceSmoothed() {
		forceSmoothed -= forceSmoothed - force;
		return forceSmoothed;
	}

	public final Particle getFirstParticle() {
		return p1;
	}

	public final Particle getSecondParticle() {
		return p2;
	}

	public final Particle getSecondParticle(Particle firstParticle) {
		return firstParticle == p1 ? p2 : p1;
	}

	public boolean isHasParticle(Particle p) {
		return (p1.equals(p) || p2.equals(p));
	}

	public double defineAngle() {
		angle = Functions.angle(p1.getX() - p2.getX(), p1.getY() - p2.getY());
		return angle;
	}

	public final double getAngle() {
		return angle;
	}

	protected final double defineVelocityProjection() {
		return (lastDistance - distance) / getInstance().timeStepController.getTimeStepSize();
	}

	public double getCriticalShift() {
		return criticalShift;
	}
	
	public double getForceValue() {
		return force;
	}

	public double getSafetyReserve() {
		return timeStepReserve;
	}

	public void doForce() {
		lastDistance = distance;
		distance = defineDistance(p1, p2);
		calculateTimeStepReserve();
		if (timeStepReserve < 1) {
			getInstance().timeStepController.setAlarm();
		}
	}
}
