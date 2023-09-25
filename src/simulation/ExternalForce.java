package simulation;

import elements.point.PointMass;

public class ExternalForce {
	private double fx, fy;


	public ExternalForce(double fx, double fy) {
		this.fx = fx;
		this.fy = fy;
	}

	public void apply(PointMass p) {
		p.addFx(fx);
		p.addFy(fy);
	}

}
