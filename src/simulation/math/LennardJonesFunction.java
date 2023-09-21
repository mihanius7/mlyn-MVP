package simulation.math;

public class LennardJonesFunction extends TabulatedFunction {

	public LennardJonesFunction(double minX, double maxX, double step) {
		super(minX, maxX, step);
	}

	@Override
	public void calculateTable() {
		double x = minX;
		PairForce force = new PairForce();
		for (int i = 0; i < functionTable.length - 1; i++) {
			x = minX + i * stepSize;
			functionTable[i] = force.LennardJones(x, param1, param2);
		}
	}

}
