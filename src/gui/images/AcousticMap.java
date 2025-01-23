package gui.images;

import calculation.Vector;
import elements.point.Particle;
import gui.viewport.Viewport;
import simulation.Simulation;

public class AcousticMap extends HeatMap {

	public AcousticMap(int w, int h) {
		super(w, h);
	}

	public AcousticMap(Viewport v) {
		super(v);
	}

	@Override
	protected double calculatePixel(double x, double y, Particle testParticle, Vector field,
			double distance, double increment) {
		increment = Math.sin((2 * Math.PI * testParticle.getMass() * 1000.0)
				* (0 * Simulation.getInstance().time() - distance / 343)) / distance;
		field.addToX(increment);
		return increment;
	}

}
