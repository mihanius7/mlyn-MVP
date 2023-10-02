package gui.viewport;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import elements.point.Particle;
import simulation.Simulation;
import simulation.components.InteractionProcessor;
import simulation.components.InteractionType;
import simulation.math.Functions;
import simulation.math.PairForce;

public class HeatMap {

	private int updateInterval = 3, resolution = 6, updatesNumber = 0, width, height;
	private Graphics2D heatMapCanvas;
	private BufferedImage heatMapImage;
	private PairForce pairForce;
	private double range = 5000;
	private double minValue, minField;
	private double maxValue, maxField;
	private boolean isGravityFieldMap;
	private boolean isAdaptiveRange = false;

	public HeatMap(int w, int h) {
		width = w;
		height = h;
		heatMapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		heatMapCanvas = heatMapImage.createGraphics();
		pairForce = new PairForce();
	}

	public HeatMap(Viewport viewport) {
		this(viewport.getWidth(), viewport.getHeight());
	}

	public BufferedImage getHeatMapImage() {
		return heatMapImage;
	}

	public void updateHeatMapImage() {
		updatesNumber++;
		if (InteractionProcessor.getInteractionType() == InteractionType.GRAVITATION)
			isGravityFieldMap = true;
		else if (InteractionProcessor.getInteractionType() == InteractionType.COULOMB
				|| InteractionProcessor.getInteractionType() == InteractionType.COULOMB_AND_GRAVITATION)
			isGravityFieldMap = false;
		int ui = (Simulation.getInstance().isActive()) ? updateInterval : 25;
		if (updatesNumber >= ui) {
			if (isAdaptiveRange) {
				minValue = Double.MAX_VALUE;
				maxValue = Double.MIN_VALUE;
				range = (Math.max(minField, maxField) - Math.min(minField, maxField)) / 4096;
				System.out.println("Range: " + range);
			}
			updatesNumber = 0;
			int wSteps = (int) (width / resolution);
			int hSteps = (int) (height / resolution);
			for (int pixelX = 0; pixelX <= wSteps; pixelX++) {
				for (int pixelY = 0; pixelY <= hSteps; pixelY++) {
					double field = defineFieldStrength(pixelX, pixelY);
					heatMapCanvas.setColor(defineColor(field));
					heatMapCanvas.fill(
							new Rectangle2D.Double(pixelX * resolution, pixelY * resolution, resolution, resolution));
					if (field > maxValue)
						maxValue = field;
					else if (field < minValue)
						minValue = field;
				}
			}
			minField = minValue;
			maxField = maxValue;
		}
	}

	private double defineFieldStrength(int pixelX, int pixelY) {
		Particle testParticle;
		double distance;
		double x1;
		double y1;
		double x2;
		double y2;
		double field = 0;
		x1 = CoordinateConverter.fromScreenX(pixelX * resolution);
		y1 = CoordinateConverter.fromScreenY(pixelY * resolution);
		x2 = CoordinateConverter.fromScreenX((pixelX + 1) * resolution);
		y2 = CoordinateConverter.fromScreenY((pixelY + 1) * resolution);
		int pNumber = 0;
		while (Simulation.getInstance().getContent().getParticle(pNumber) != null) {
			testParticle = Simulation.getInstance().getContent().getParticle(pNumber);
			distance = Functions.defineDistance(testParticle, (x1 + x2) / 2, (y1 + y2) / 2);
			field += (isGravityFieldMap) ? pairForce.defineGravitationFieldStrength(testParticle, distance)
					: pairForce.defineCoulombFieldStrength(testParticle, distance);
			pNumber++;
		}
		return field;
	}

	public Color defineColor(double value) {
		Color c1;
		int colorIndex;
		colorIndex = (isGravityFieldMap) ? (int) Functions.linear2DInterpolation(-maxField, 255, 0, 0, value)
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

}
