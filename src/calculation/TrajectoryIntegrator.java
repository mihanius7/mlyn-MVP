package calculation;

import elements.point.PointMass;
import simulation.Simulation;

public class TrajectoryIntegrator {

	public void calculateNextLocation(PointMass pm) {
		pm.addX(pm.getVelocityVector().X() * Simulation.getInstance().timeStepController.getTimeStepSize()
				+ pm.isMovableX()
						* (pm.getForceVector().X() * Simulation.getInstance().timeStepController.getTimeStepSize()
								* Simulation.getInstance().timeStepController.getTimeStepSize())
						/ (2 * pm.getMass()));
		pm.addY(pm.getVelocityVector().Y() * Simulation.getInstance().timeStepController.getTimeStepSize()
				+ pm.isMovableY()
						* (pm.getForceVector().Y() * Simulation.getInstance().timeStepController.getTimeStepSize()
								* Simulation.getInstance().timeStepController.getTimeStepSize())
						/ (2 * pm.getMass()));
	}

	public void calculateNextVelocity(PointMass pm) {
		pm.getLastVelocityVector().setX(pm.getVelocityVector().X());
		pm.getLastVelocityVector().setY(pm.getVelocityVector().Y());
		if (pm.isMovable()) {
			if (pm.isMoving()) {
				if (Simulation.getInstance().interactionProcessor.isUseFriction()) {
					pm.getForceVector().addToXY(
							-pm.getVelocityVector().X()
									* Simulation.getInstance().interactionProcessor.getAirFrictionCoefficient(),
							-pm.getVelocityVector().Y()
									* Simulation.getInstance().interactionProcessor.getAirFrictionCoefficient());
					pm.getForceVector().addToXY(-pm.getVelocityVector().defineCos() * pm.getFrictionForce(),
							-pm.getVelocityVector().defineSin() * pm.getFrictionForce());
				}
				velocityVerlet(pm, Simulation.getInstance().timeStepController.getTimeStepSize());
			} else {
				if (Simulation.getInstance().interactionProcessor.isUseFriction() && pm.isStictionReached())
					velocityVerlet(pm, Simulation.getInstance().timeStepController.getTimeStepSize());
				else
					velocityVerlet(pm, Simulation.getInstance().timeStepController.getTimeStepSize());
			}
		}
	}

	private void velocityVerlet(PointMass pm, double dt) {
		pm.getVelocityVector().addToX(pm.isMovableX()
				* (dt * (pm.getLastForceVector().X() + pm.getForceVector().X()) / (2.0 * pm.getMass())));
		pm.getVelocityVector().addToY(pm.isMovableY()
				* (dt * (pm.getLastForceVector().Y() + pm.getForceVector().Y()) / (2.0 * pm.getMass())));
	}
}
