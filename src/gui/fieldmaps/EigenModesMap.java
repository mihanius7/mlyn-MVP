package gui.fieldmaps;

import calculation.Vector;
import elements.point.Particle;
import gui.viewport.Colors;
import gui.viewport.Viewport;
import simulation.Simulation;

public class EigenModesMap extends WavesMap {

	private double z = 1.6;
	private double a, b, c;
//	private byte kmax = 0;
//	private byte lmax = 0;
//	private byte mmax = 0;

	private byte modes[][] = { { 2, 0, 0 }, { 1, 0, 0 }, { 0, 1, 0 }};

	public EigenModesMap(Viewport v) {
		super(v);
	}

	public EigenModesMap(int w, int h) {
		super(w, h);
	}

	@Override
	protected double calculatePixel(double x, double y, Particle testParticle, Vector field, double distance,
			double amplitude) {
		for (int i = 0; i < modes.length; i++) {
			amplitude += Math.cos(modes[i][0] * Math.PI * x / a) * Math.cos(modes[i][1] * Math.PI * y / b)
					* Math.cos(modes[i][2] * Math.PI * z / c);
		}
//		for (int k = 0; k <= kmax; k++) {
//			for (int l = 0; l <= lmax; l++) {
//				for (int m = 0; m <= mmax; m++) {
//					amplitude += Math.cos(k * Math.PI * x / a) * Math.cos(l * Math.PI * y / b)
//							* Math.cos(m * Math.PI * z / c);
//				}
//			}
//		}
		field.addToX(amplitude);
		return amplitude;
	}

	@Override
	public void setDefaultParameters() {
		isAdaptiveRange = false;
		range = 5;
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
