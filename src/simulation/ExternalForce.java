package simulation;

import elements.point.Particle;

public class ExternalForce {
	private double fx, fy;


	public ExternalForce(double fx, double fy) {
		this.fx = fx;
		this.fy = fy;
	}

	public void apply(Particle p) {
		p.addFx(fx);
		p.addFy(fy);
	}

}
