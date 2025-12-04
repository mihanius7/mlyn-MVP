package calculation.pairforce;

import static calculation.constants.PhysicalConstants.cm;
import static calculation.constants.PhysicalConstants.dj;

import calculation.pairforce.tabulated.LennardJonesPotential;
import elements.point.Particle;

public class LennardJones extends PairForce {

	LennardJonesPotential table;

	public LennardJones() {
		table = new LennardJonesPotential(0.5 * cm, 150 * cm, 1 * cm);
		table.setParam1(14 * cm);
		table.setParam2(1 * dj);
		//table.calculateTable();
	}


	@Override
	public double calculatePotential(Particle p1, double r) {
		return table.calculatePotential(table.getParam1(), table.getParam2(), r);
	}

	@Override
	public double calculateForce(Particle p1, Particle p2, double r) {
		return table.calculateForce(table.getParam1(), table.getParam2(), r);
	}

	@Override
	public double calculateStrength(Particle p1, double r) {
		return table.calculatePotential(table.getParam1(), table.getParam2(), r);
	}

	@Override
	public double calculatePotentialEnergy(Particle p1, Particle p2, double r) {
		return -1;
	}

	@Override
	public double distanceLimit() {
		return table.getMaxArgument() * 2.5;
	}

}
