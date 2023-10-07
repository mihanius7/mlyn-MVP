package calculation.pairforce;

import elements.point.Particle;

public abstract class PairForce {
	
	public abstract double calculatePotential(Particle p1, double r);
	
	public abstract double calculateForce(Particle p1, Particle p2, double r);
	
	public abstract double calculateStrength(Particle p1, double r);
	
	public abstract double calculatePotentialEnergy(Particle p1, Particle p2, double r);
	
	public double distanceLimit() {
		return Double.MAX_VALUE;
	}
	
}
