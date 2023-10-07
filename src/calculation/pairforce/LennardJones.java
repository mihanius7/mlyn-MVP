package calculation.pairforce;

import static calculation.constants.PhysicalConstants.cm;
import static calculation.constants.PhysicalConstants.dj;

import calculation.pairforce.tabulated.LennardJonesTable;
import elements.point.Particle;

public class LennardJones extends PairForce {

	LennardJonesTable table;

	public LennardJones() {
		table = new LennardJonesTable(0.1, 150 * cm, 0.1 * cm);
		table.setParam1(20 * cm);
		table.setParam2(40 * dj);
		table.calculateTable();
	}

	public double calculateForce(double a, double b, double r) {
		return table.getFromTable(r);
	}

	@Override
	public double calculatePotential(Particle p1, double r) {
		return 0;
	}

	@Override
	public double calculateForce(Particle p1, Particle p2, double r) {
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

	@Override
	public double distanceLimit() {
		return table.getMaxArgument() * 2.5;
	}

}
