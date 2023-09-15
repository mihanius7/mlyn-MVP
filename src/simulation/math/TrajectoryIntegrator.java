package simulation.math;

import elements.point.Particle;
import simulation.Simulation;

public class TrajectoryIntegrator {
	
	public void calculateNextVelocity(Particle p, double dt, boolean useFriction) {
		p.getLastVelocityVector().setX(p.getVelocityVector().X());
		p.getLastVelocityVector().setY(p.getVelocityVector().Y());
		if (p.isMoving()) {
			if (useFriction) {
				p.getForceVector().addToXY(-p.getVelocityVector().X() * Simulation.getInstance().interactionProcessor.getAirFrictionCoefficient(), -p.getVelocityVector().Y() * Simulation.getInstance().interactionProcessor.getAirFrictionCoefficient());
				p.getForceVector().addToXY(-p.getVelocityVector().defineCos() * p.getFrictionForce(), -p.getVelocityVector().defineSin() * p.getFrictionForce());
			}
			velocityVerlet(p, dt);
		} else {
			if (useFriction && p.isStictionReached())
				velocityVerlet(p, dt);
			else
				velocityVerlet(p, dt);
		}
	}

	private void velocityVerlet(Particle p, double dt) {
		p.getVelocityVector().addToX(p.isMovableX() * (dt * (p.getLastForceVector().X() + p.getForceVector().X()) / (2.0 * p.getMass())));
		p.getVelocityVector().addToY(p.isMovableY() * (dt * (p.getLastForceVector().Y() + p.getForceVector().Y()) / (2.0 * p.getMass())));
	}
}
