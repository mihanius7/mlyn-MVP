package gui.fieldmaps;

import calculation.Vector;
import elements.point.Particle;
import gui.viewport.Colors;
import gui.viewport.Viewport;

public class WavesMap extends FieldMap {

	private boolean overrideFreq = true;
	private boolean distanceDecay = true;
	private double sourceFrequency = 440;
	private double freq;
	private double waveSpeed = 343;

	public WavesMap(int w, int h) {
		super(w, h);
		setDefaultParameters();
	}

	public WavesMap(Viewport v) {
		super(v);
		setDefaultParameters();
	}

	@Override
	protected double calculatePixel(double x, double y, Particle testParticle, Vector field, double distance,
			double increment) {
		freq = overrideFreq ? sourceFrequency : testParticle.getMass() * 1000.0;
		increment = -0.05 * Math.sin(2 * Math.PI * freq * distance / waveSpeed);
		if (distanceDecay)
			increment /= distance;
		field.addToX(increment);
		return increment;
	}

	public boolean isOverrideFreq() {
		return overrideFreq;
	}

	public void setOverrideFreq(boolean overrideFreq) {
		this.overrideFreq = overrideFreq;
	}

	public boolean isDistanceDecay() {
		return distanceDecay;
	}

	public void setDistanceDecay(boolean distanceDecay) {
		this.distanceDecay = distanceDecay;
	}

	public double getSourceFrequency() {
		return sourceFrequency;
	}

	public void setSourceFrequency(double f) {
		if (f > 0)
			this.sourceFrequency = f;
	}

	public double getWaveSpeed() {
		return waveSpeed;
	}

	public void setWaveSpeed(double v) {
		if (v > 0)
			this.waveSpeed = v;
	}
	
	public void setDefaultParameters() {
		range = 0.5;
		resolution = waveSpeed / sourceFrequency / 10;
		palette = Colors.BWR;
		projectionType = ProjectionType.X;
		isAdaptiveRange = false;
	}
	
	public String getCrosshairTagFor(double value) {
		return String.format("SPL = %.1f [dB]", 20 * Math.log10(Math.max(value, 0.00002) / 0.00002));
	}

}
