package simulation.math.force;

import static constants.PhysicalConstants.k;
import elements.point.Particle;

public class Coulomb extends CentralForce {

	@Override
	public double calculatePotential(Particle p1, double r) {
		return k * p1.getCharge() / r;
	}

	@Override
	public double calculateForce(Particle p1, Particle p2, double r) {
		return calculatePotential(p1, r) * p2.getCharge() / r;
	}

	@Override
	public double calculateStrength(Particle p1, double r) {
		return calculatePotential(p1, r) / r;
	}

	@Override
	public double calculatePotentialEnergy(Particle p1, Particle p2, double r) {
		return calculatePotential(p1, r) * p2.getCharge();
	}

	@Override
	public double distanceLimit() {
		return 15.0;
	}	

}
