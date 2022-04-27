package elements.force_pair;

import static evaluation.MyMath.defineDistance;
import static java.lang.Math.min;
import static simulation.Simulation.timeStepController;
import elements.point_mass.Particle;
import evaluation.MyMath;
import gui.MainWindow;
import simulation.Simulation;

public abstract class ForcePair implements ForceElement {

	protected final Particle p1, p2;
	protected double force, oldForceSmoothed;
	protected double distance, lastDistance = 0;
	protected double criticalShift, angle;
	private double timeStepReserve;

	public ForcePair() {
		p1 = null;
		p2 = null;
	}

	public ForcePair(Particle i, Particle j) {
		p1 = Simulation.getParticleWithLesserIndex(i, j);
		p2 = Simulation.getParticleWithLargerIndex(i, j);
		setCriticalShift();
		distance = lastDistance + criticalShift;
	}

	public ForcePair(int i, int j) {
		p1 = Simulation.getParticleWithLesserIndex(Simulation.getParticle(i), Simulation.getParticle(j));
		p2 = Simulation.getParticleWithLargerIndex(Simulation.getParticle(i), Simulation.getParticle(j));
		setCriticalShift();
		distance = lastDistance + criticalShift;
	}

	private void setCriticalShift() {
		criticalShift = 0.25 * min(p1.getRadius(), p2.getRadius());
	}

	private void refreshTimeStepReserve() {
		timeStepReserve = criticalShift / Math.abs(lastDistance - distance);
	}

	public final double getForce() {
		return force;
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
		angle = MyMath.angle(p1.getX() - p2.getX(), p1.getY() - p2.getY());
		return angle;
	}

	public final double getAngle() {
		return angle;
	}

	protected final double defineVelocityProjection() {
		double p = (lastDistance - distance) / timeStepController.getTimeStepSize();
		return p;
	}

	public double getReserveRatio() {
		return timeStepReserve;
	}

	public double getCriticalShift() {
		return criticalShift;
	}

	public void applyForce() {
		lastDistance = distance;
		distance = defineDistance(p1, p2);
		refreshTimeStepReserve();
		if (timeStepReserve < 1) {
			timeStepController.setTimeStepAlarm();
		}
	}
}
