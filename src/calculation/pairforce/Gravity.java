package calculation.pairforce;

import static calculation.constants.PhysicalConstants.G;

import elements.point.Particle;

public class Gravity extends PairForce {

	@Override
	public double calculatePotential(Particle p1, double r) {
		return G * p1.getMass() / r;
	}

	@Override
	public double calculateForce(Particle p1, Particle p2, double r) {
		return calculatePotential(p1, r) * p2.getMass() / r;
	}

	@Override
	public double calculateStrength(Particle p1, double r) {
		return calculatePotential(p1, r) / r;
	}

	@Override
	public double calculatePotentialEnergy(Particle p1, Particle p2, double r) {
		return calculatePotential(p1, r) * p2.getMass();
	}

}