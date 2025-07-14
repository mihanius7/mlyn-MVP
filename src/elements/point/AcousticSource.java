package elements.point;

import calculation.constants.PhysicalConstants;

public class AcousticSource extends Point {

	protected double frequency, pressureAmplitude;
	protected double radius = 5 * PhysicalConstants.cm;

	public AcousticSource(double x, double y, double hz) {
		super(x, y);
		this.frequency = hz;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double hz) {
		this.frequency = hz;
	}

	public double getPressureAmplitude() {
		return pressureAmplitude;
	}

	public void setPressureAmplitude(double pascals) {
		this.pressureAmplitude = pascals;
	}

}
