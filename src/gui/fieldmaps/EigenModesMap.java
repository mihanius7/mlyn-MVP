package gui.fieldmaps;

import calculation.Vector;
import elements.point.Particle;
import gui.viewport.Colors;
import gui.viewport.Viewport;
import simulation.Simulation;

public class EigenModesMap extends WavesMap {

	private double z = 2.44 / 5 * 4;
	private double a, b, c;
	private byte kmax = 2;
	private byte lmax = 0;
	private byte mmax = 0;

	public EigenModesMap(Viewport v) {
		super(v);
	}

	public EigenModesMap(int w, int h) {
		super(w, h);
	}

	@Override
	protected double calculatePixel(double x, double y, Particle testParticle, Vector field, double distance,
			double increment) {
		for (int k = 0; k <= kmax; k++) {
			for (int l = 0; l <= lmax; l++) {
				for (int m = 0; m <= mmax; m++) {
					increment += Math.cos(k * Math.PI * x / a) * Math.cos(l * Math.PI * y / b)
							* Math.cos(m * Math.PI * z / c);
				}
			}
		}
		field.addToX(increment);
		return increment;
	}

	@Override
	public void setDefaultParameters() {
		isAdaptiveRange = true;
		range = 15;
		resolution = 0.09;
		palette = Colors.BWR;
		projectionType = ProjectionType.X;
		a = Simulation.getInstance().content().getBoundaries().getWidth();
		b = Simulation.getInstance().content().getBoundaries().getHeight();
		c = 2.44;
	}

	@Override
	public String getCrosshairTagFor(double value) {
		return String.format("P = %.3f [Pa]", value);
	}

}
