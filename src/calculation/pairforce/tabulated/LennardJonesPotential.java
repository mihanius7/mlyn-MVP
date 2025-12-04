package calculation.pairforce.tabulated;

public class LennardJonesPotential extends TabulatedFunction {

	public LennardJonesPotential(double minR, double maxR, double dr) {
		super(minR, maxR, dr);
	}

	public double calculatePotential(double sigma, double epsilon, double r) {
		return 4 * epsilon * (Math.pow(sigma / r, 12) - Math.pow(sigma / r, 6));
	}
	
	public double calculateForce(double sigma, double epsilon, double r) {
		return 24 * epsilon * (2 * Math.pow(sigma / r, 12) - Math.pow(sigma / r, 6));
	}

	@Override
	public void calculateTable() {
		double r = minX;
		for (int i = 0; i < functionTable.length - 1; i++) {
			r = minX + i * stepSize;
			functionTable[i] = calculatePotential(param1, param2, r);
			System.out.println(String.format("%.3f; %.3f", r, functionTable[i]));
		}
	}

}
