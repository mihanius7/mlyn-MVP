package elements;

import elements.point_mass.Particle;
import simulation.components.SimulationComponent;

public class StaticForce implements SimulationComponent {
	private Particle p;
	private double fx, fy;

	public StaticForce(Particle target, double fx, double fy) {
		this.p = target;
		this.fx = fx;
		this.fy = fy;
	}

	@Override
	public void process() {
		p.addFx(fx);
		p.addFy(fy);
		p.getLastForceVector().setXY(fx, fy);
	}

	@Override
	public void setSkipStepsNumber(int skip) {
	}

}
