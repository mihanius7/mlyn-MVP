package gui.fieldmaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import calculation.Functions;
import calculation.Vector;
import calculation.pairforce.PairForce;
import calculation.pairforce.PairForceFactory;
import elements.point.Particle;
import gui.viewport.Colors;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Boundaries;
import simulation.Simulation;

public class FieldMap {

	public static final int MIN_PIXEL_SIZE = 3;
	public static final double MINIMAL_DISTANCE_COEF = 0.75;
	public static final float AUTORANGE_VALUE_DIVIDER = 5f;
	protected int updateInterval = 2, updatesNumber = 0, width, height;
	private Graphics2D fieldMapCanvas;
	private BufferedImage fieldMapImage;
	protected double range = 5E5;
	protected double resolution = 0.025;
	protected double minValue, minField;
	protected double maxValue, maxField;
	protected boolean isAdaptiveRange = true;
	protected FieldType fieldType = FieldType.STRENGTH;
	protected ProjectionType projectionType = ProjectionType.MAGNITUDE;
	protected Boundaries b;
	protected PairForce pairForce;
	protected int palette[][] = Colors.WBW;

	public FieldMap(int w, int h) {
		b = Simulation.getInstance().content().getBoundaries();
		width = Math.min(w, CoordinateConverter.toScreen(b.getWidth()));
		height = Math.min(h, CoordinateConverter.toScreen(b.getHeight()));
		fieldMapImage = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_RGB);
		fieldMapCanvas = fieldMapImage.createGraphics();
	}

	public FieldMap(Viewport v) {
		this(v.getWidth(), v.getHeight());
	}

	public BufferedImage getImage() {
		return fieldMapImage;
	}

	public void updateImage() {
		updatesNumber++;
		int ui = (Simulation.getInstance().isActive()) ? updateInterval : 15;
		if (updatesNumber >= ui) {
			adjustRange();
			updatesNumber = 0;
			int dh = Math.max(CoordinateConverter.toScreen(resolution), MIN_PIXEL_SIZE);
			int wSteps = (int) (width / dh);
			int hSteps = (int) (height / dh);
			int x0 = (int) Math.max(0, CoordinateConverter.toScreenX(b.getLeft()));
			int y0 = (int) Math.max(0, CoordinateConverter.toScreenY(b.getUpper()));
			for (int stepX = 0; stepX <= wSteps; stepX++) {
				for (int stepY = 0; stepY <= hSteps; stepY++) {
					double xc = (CoordinateConverter.fromScreenX(stepX * dh + x0)
							+ CoordinateConverter.fromScreenX((stepX + 1) * dh + x0)) / 2;
					double yc = (CoordinateConverter.fromScreenY(stepY * dh + y0)
							+ CoordinateConverter.fromScreenY((stepY + 1) * dh + y0)) / 2;
					Vector fieldVector = new Vector();
					fieldVector = calculateField(xc, yc);
					double fieldValue = defineProjection(fieldVector);
					fieldMapCanvas.setColor(defineColor(fieldValue, range));
					fieldMapCanvas.fill(new Rectangle2D.Double(stepX * dh, stepY * dh, dh, dh));
//					viewport.drawArrowLine(heatMapCanvas, (int) (stepX + 0.5) * resolution, (int) (stepY + 0.5) * resolution,
//							fieldVector.multiply(0.5), Color.BLACK, "");
					if (fieldValue > maxValue)
						maxValue = fieldValue;
					else if (fieldValue < minValue)
						minValue = fieldValue;
				}
			}
			minField = minValue;
			maxField = maxValue;
		}
	}

	private void adjustRange() {
		if (isAdaptiveRange) {
			minValue = Double.MAX_VALUE;
			maxValue = Double.MIN_VALUE;
			range = (Math.max(minField, maxField) - Math.min(minField, maxField)) / AUTORANGE_VALUE_DIVIDER;
		}
	}

	private double defineProjection(Vector v) {
		double result = 0;
		if (projectionType == ProjectionType.MAGNITUDE)
			result = v.norm();
		else if (projectionType == ProjectionType.X)
			result = v.X();
		if (projectionType == ProjectionType.Y)
			result = v.Y();
		return result;
	}

	public Color defineColor(double value, double range) {
		Color c1;
		int colorIndex;
		colorIndex = (int) Functions.linear2DInterpolation(-range / 2, 0, range / 2, 255, value);
		c1 = new Color(palette[colorIndex][0], palette[colorIndex][1], palette[colorIndex][2]);
		return c1;
	}

	private Vector calculateField(double x, double y) {
		Particle testParticle;
		pairForce = PairForceFactory
				.getCentralForce(Simulation.getInstance().interactionProcessor.getInteractionType());
		Vector field = new Vector();
		double distance;
		double increment = 0;
		int pNumber = 0;
		while (Simulation.getInstance().content().particle(pNumber) != null) {
			testParticle = Simulation.getInstance().content().particle(pNumber);
			distance = Functions.defineDistance(testParticle, x, y);
			if (distance >= MINIMAL_DISTANCE_COEF * testParticle.getRadius()) {
				increment = calculatePixel(x, y, testParticle, field, distance, increment);
			}
			pNumber++;
		}
		return field;
	}

	protected double calculatePixel(double x, double y, Particle testParticle, Vector field,
			double distance, double increment) {
		if (fieldType == FieldType.POTENTIAL)
			increment = pairForce.calculatePotential(testParticle, distance);
		else if (fieldType == FieldType.STRENGTH)
			increment = pairForce.calculateStrength(testParticle, distance);
		field.addToX(increment * (x - testParticle.getX()) / distance);
		field.addToY(increment * (y - testParticle.getY()) / distance);
		return increment;
	}

	public Vector calculateField(int px, int py) {
		return calculateField(CoordinateConverter.fromScreenX(px), CoordinateConverter.fromScreenY(py));
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public ProjectionType getProjectionType() {
		return projectionType;
	}

	public void setProjectionType(ProjectionType projectionType) {
		this.projectionType = projectionType;
	}

	public String getCrosshairTagFor(double value) {
		return String.format("|E| = %.1e [V/m]", value);
	}

}
