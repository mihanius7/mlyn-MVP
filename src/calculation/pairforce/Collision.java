package calculation.pairforce;

import static calculation.constants.PhysicalConstants.cm;
import static java.lang.Math.sqrt;

import elements.point.Particle;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;

public class Collision extends PairForce {

	private double beta = (2.0 * 1E6 * sqrt(5 * cm * 5 * cm / (5 * cm + 5 * cm))) / (3 * (1 - 0.28 * 0.28));

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

	@Override
	public double calculateForce(Particle p1, Particle p2, double r) {
		return beta * Math.pow(p1.getRadius() + p2.getRadius() - r, 1.5);
	}

	@Override
	public double calculatePotential(Particle p1, double r) {
		return 0;
	}

	@Override
	public double calculateStrength(Particle p1, double r) {
		return 0;
	}

	@Override
	public double calculatePotentialEnergy(Particle p1, Particle p2, double r) {
		return 0;
	}

}
