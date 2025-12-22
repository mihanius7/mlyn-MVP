package gui.fieldmaps;

import calculation.Vector;
import elements.point.Particle;
import gui.viewport.Colors;
import gui.viewport.Viewport;

public class SPLMap extends PhysicalFieldMap {

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
		if (fieldType == FieldParameter.POTENTIAL)
			increment = pairForce.calculatePotential(testParticle, distance);
		else if (fieldType == FieldParameter.STRENGTH)
			increment = pairForce.calculateStrength(testParticle, distance);
		field.addToX(increment * (x - testParticle.getX()) / distance);
		field.addToY(increment * (y - testParticle.getY()) / distance);
		return increment;
	}

	@Override
	public void setDefaultParameters() {
		isAdaptiveRange = false;
		range = 5;
		resolution = 0.1;
		palette = Colors.BWR;
		projectionType = ProjectionType.X;
	}

}
