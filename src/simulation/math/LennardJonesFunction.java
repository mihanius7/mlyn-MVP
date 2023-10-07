package simulation.math;

import simulation.math.force.CentralForce;

public class LennardJonesFunction extends TabulatedFunction {

	public LennardJonesFunction(double minX, double maxX, double step) {
		super(minX, maxX, step);
	}

	@Override
	public void calculateTable() {
		double x = minX;
		CentralForce force = new CentralForce();
		for (int i = 0; i < functionTable.length - 1; i++) {
			x = minX + i * stepSize;
			functionTable[i] = force.LennardJones(x, param1, param2);
		}
	}

}
