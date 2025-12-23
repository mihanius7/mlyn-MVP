package gui.fieldmaps;

import calculation.Vector;
import elements.point.Particle;
import gui.viewport.Colors;
import gui.viewport.Viewport;

public class SPLMap extends StandingWavesMap {

	public SPLMap(int w, int h) {
		super(w, h);
		setDefaultParameters();
	}

	public SPLMap(Viewport v) {
		super(v);
		setDefaultParameters();
	}

	@Override
	protected double calculatePixel(double x, double y, Particle testParticle, Vector field, double distance,
			double increment) {
		increment = testParticle.getMass() / distance / distance;
		field.addToX(increment);
		return increment;
	}

	@Override
	public void setDefaultParameters() {
		isAdaptiveRange = false;
		range = 1000;
		resolution = 0.1;
		palette = Colors.TURBO;
		projectionType = ProjectionType.X;
	}

}
