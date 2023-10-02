package simulation.math;

import static constants.PhysicalConstants.G;
import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.k;
import static java.lang.Math.sqrt;
import static simulation.math.Functions.sqr;

import elements.point.Particle;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;

public class PairForce {
	
	private double beta = (2.0 * 1E6 * sqrt(5 * cm * 5 * cm / (5 * cm + 5 * cm))) / (3 * (1 - 0.28 * 0.28));
	
	public double defineCoulombForce(Particle particle1, Particle particle2, double distance) {
		return k * particle1.getCharge() * particle2.getCharge() / sqr(distance);
	}

	public double defineCoulombForceSq(Particle particle1, Particle particle2, double squaredDistance) {
		return k * particle1.getCharge() * particle2.getCharge() / squaredDistance;
	}

	public double defineCoulombFieldStrength(Particle particle, double distance) {
		return k * particle.getCharge() / sqr(distance);
	}

	public double defineGravitationForce(Particle particle1, Particle particle2, double distance) {
		return -G * particle1.getMass() * particle2.getMass() / sqr(distance);
	}

	public double defineGravitationFieldStrength(Particle particle1, double distance) {
		return -G * particle1.getMass() / sqr(distance);
	}
	
	public double defineHertzForce(double depth) {
		return beta * Math.pow(depth, 1.5);
	}
	
	public void setBeta(double r1, double r2, double e, double u) {
		this.beta = (2 * e * sqrt(r1 * r2 / (r1 + r2))) / (3 * (1 - u * u));
		ConsoleWindow.println(String.format(GUIStrings.RECOIL_BY_HERTZ + ", beta = %.3e", beta));
	}

	public void setBeta(double r1, double r2, double e1, double e2, double u1, double u2) {
		this.beta = 4 / 3 / (1 / e1 - u1 * u1 / e1 + 1 / e2 - u2 * u2 / e2) / sqrt(1 / r1 + 1 / r2);
		ConsoleWindow.println(String.format(GUIStrings.RECOIL_BY_HERTZ + ", beta = %.3e", beta));
	}

	public void setBeta(double beta) {
		this.beta = beta;
		ConsoleWindow.println(String.format(GUIStrings.RECOIL_BY_HERTZ + ", beta = %.3e", beta));
	}
	
	public double LennardJones(double r, double a, double d) {
		return 12 * d / a * (Math.pow(a / r, 10) - Math.pow(a / r, 7));
	}
}
