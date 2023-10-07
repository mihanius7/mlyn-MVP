package simulation.math;

public class LennardJonesTable extends TabulatedFunction {

	public LennardJonesTable(double minX, double maxX, double step) {
		super(minX, maxX, step);
	}
	
	public double calculateForce(double d, double a, double r) {
		return 12 * d / a * (Math.pow(a / r, 10) - Math.pow(a / r, 7));
	}

	@Override
	public void calculateTable() {
		double r = minX;
		for (int i = 0; i < functionTable.length - 1; i++) {
			r = minX + i * stepSize;
			functionTable[i] = calculateForce(param1, param2, r);
		}
	}

}
