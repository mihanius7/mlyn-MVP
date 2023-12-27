package gui.images;

import static simulation.Simulation.getInstance;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import calculation.Functions;
import calculation.Vector;
import gui.viewport.Colors;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Simulation;
import simulation.components.InteractionType;

public class HeatMap {

	private static final int AUTORANGE_VALUE_DIVIDER = 2;
	private int updateInterval = 5, resolution = 8, updatesNumber = 0, width, height;
	private Graphics2D heatMapCanvas;
	private BufferedImage heatMapImage;
	private double range = 1000;
	private double minValue, minField;
	private double maxValue, maxField;
	private boolean isGravityFieldMap;
	private boolean isAdaptiveRange = true;
	private Viewport viewport;
	private FieldType fieldType = FieldType.STRENGTH;
	private ProjectionType projectionType = ProjectionType.MAGNITUDE;

	public HeatMap(int w, int h) {
		width = w;
		height = h;
		heatMapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		heatMapCanvas = heatMapImage.createGraphics();
	}

	public HeatMap(Viewport v) {
		this(v.getWidth(), v.getHeight());
		this.viewport = v;
	}

	public BufferedImage getImage() {
		return heatMapImage;
	}

	public void updateImage() {
		updatesNumber++;
		if (getInstance().interactionProcessor.getInteractionType() == InteractionType.GRAVITATION)
			isGravityFieldMap = true;
		else if (getInstance().interactionProcessor.getInteractionType() == InteractionType.COULOMB)
			isGravityFieldMap = false;
		int ui = (Simulation.getInstance().isActive()) ? updateInterval : 15;
		if (updatesNumber >= ui) {
			adjustRange();
			updatesNumber = 0;
			int wSteps = (int) (width / resolution);
			int hSteps = (int) (height / resolution);
			for (int stepX = 0; stepX <= wSteps; stepX++) {
				for (int stepY = 0; stepY <= hSteps; stepY++) {
					double xc = (CoordinateConverter.fromScreenX(stepX * resolution)
							+ CoordinateConverter.fromScreenX((stepX + 1) * resolution)) / 2;
					double yc = (CoordinateConverter.fromScreenY(stepY * resolution)
							+ CoordinateConverter.fromScreenY((stepY + 1) * resolution)) / 2;
					Vector fieldVector = Simulation.getInstance().interactionProcessor.calculateField(xc, yc, fieldType);
					double fieldValue = defineProjection(fieldVector);
					heatMapCanvas.setColor(defineColor(fieldValue, range));
					heatMapCanvas.fill(
							new Rectangle2D.Double(stepX * resolution, stepY * resolution, resolution, resolution));
//					viewport.drawArrowLine(heatMapCanvas, (int) (stepX + 0.5) * resolution, (int) (stepY + 0.5) * resolution,
//							fieldVector.multiply(0.0005), Color.BLACK, "");
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

	public double defineProjection(Vector v) {
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
		colorIndex = (isGravityFieldMap) ? (int) Functions.linear2DInterpolation(0, 0, range, 255, value)
				: (int) Functions.linear2DInterpolation(-range / 2, 0, range / 2, 255, value);
		c1 = new Color(Colors.TURBO_SRGB_BYTES[colorIndex][0], Colors.TURBO_SRGB_BYTES[colorIndex][1],
				Colors.TURBO_SRGB_BYTES[colorIndex][2]);
		return c1;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public int getResolution() {
		return resolution;
	}

	public void setResolution(int resolution) {
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

}
