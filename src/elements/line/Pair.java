package elements.line;

import static java.lang.Math.min;
import static simulation.math.Functions.defineDistance;

import elements.Movable;
import elements.point.Particle;
import simulation.Simulation;
import simulation.math.Functions;

public class Pair implements Movable {

	protected final Particle p1, p2;
	protected double force, oldForceSmoothed;
	protected double distance, lastDistance = 0;
	protected double criticalShift, angle;
	private double timeStepReserve;

	public Pair() {
		p1 = null;
		p2 = null;
	}

	public Pair(Particle i, Particle j) {
		p1 = Simulation.getInstance().getContent().getParticleWithLesserIndex(i, j);
		p2 = Simulation.getInstance().getContent().getParticleWithLargerIndex(i, j);
		setCriticalShift();
		distance = lastDistance + criticalShift;
	}

	public Pair(int i, int j) {
		p1 = Simulation.getInstance().getContent().getParticleWithLesserIndex(Simulation.getInstance().getContent().getParticle(i), Simulation.getInstance().getContent().getParticle(j));
		p2 = Simulation.getInstance().getContent().getParticleWithLargerIndex(Simulation.getInstance().getContent().getParticle(i), Simulation.getInstance().getContent().getParticle(j));
		setCriticalShift();
		distance = lastDistance + criticalShift;
	}

	private void setCriticalShift() {
		criticalShift = 0.25 * min(p1.getRadius(), p2.getRadius());
	}

	private void refreshTimeStepReserve() {
		timeStepReserve = criticalShift / Math.abs(lastDistance - distance);
	}

	public double getForceSmoothed() {
		oldForceSmoothed -= oldForceSmoothed - force;
		return oldForceSmoothed;
	}

	public final Particle getFirstParticle() {
		return p1;
	}

	public final Particle getSecondParticle() {
		return p2;
	}

	public final Particle getOppositeParticle(Particle p) {
		Particle returnParticle = null;
		if (p == p1)
			returnParticle = p2;
		else if (p == p2)
			returnParticle = p1;
		return returnParticle;
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
		double p = (lastDistance - distance) / Simulation.getInstance().timeStepController.getTimeStepSize();
		return p;
	}

	public double getCriticalShift() {
		return criticalShift;
	}

	@Override
	public void doMovement() {
		lastDistance = distance;
		distance = defineDistance(p1, p2);
		refreshTimeStepReserve();
		if (timeStepReserve < 1) {
			Simulation.getInstance().timeStepController.setTimeStepAlarm();
		}
	}

	@Override
	public double getForceValue() {
		return force;
	}

	@Override
	public double getSafetyReserve() {
		return timeStepReserve;
	}
}
