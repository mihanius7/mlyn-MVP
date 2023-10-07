package gui.viewport;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import calculation.Functions;
import calculation.Vector;
import calculation.pairforce.PairForce;
import calculation.pairforce.PairForceFactory;
import elements.point.Particle;
import simulation.Simulation;
import simulation.components.InteractionProcessor;
import simulation.components.InteractionType;

import static simulation.Simulation.getInstance;

public class HeatMap {

	private int updateInterval = 3, resolution = 10, updatesNumber = 0, width, height;
	private Graphics2D heatMapCanvas;
	private BufferedImage heatMapImage;
	private PairForce pairForce;
	private double range = 10000;
	private double minValue, minField;
	private double maxValue, maxField;
	private boolean isGravityFieldMap;
	private boolean isAdaptiveRange = true;
	private Viewport viewport;

	public HeatMap(int w, int h) {
		width = w;
		height = h;
		heatMapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		heatMapCanvas = heatMapImage.createGraphics();
		pairForce = PairForceFactory.getCentralForce(getInstance().interactionProcessor.getInteractionType());
	}

	public HeatMap(Viewport v) {
		this(v.getWidth(), v.getHeight());
		this.viewport = v;
	}

	public BufferedImage getHeatMapImage() {
		return heatMapImage;
	}

	public void updateHeatMapImage() {
		updatesNumber++;
		if (getInstance().interactionProcessor.getInteractionType() == InteractionType.GRAVITATION)
			isGravityFieldMap = true;
		else if (getInstance().interactionProcessor.getInteractionType() == InteractionType.COULOMB)
			isGravityFieldMap = false;
		int ui = (Simulation.getInstance().isActive()) ? updateInterval : 15;
		if (updatesNumber >= ui) {
			defineRange();
			updatesNumber = 0;
			int wSteps = (int) (width / resolution);
			int hSteps = (int) (height / resolution);
			for (int stepX = 0; stepX <= wSteps; stepX++) {
				for (int stepY = 0; stepY <= hSteps; stepY++) {
					double xc = (CoordinateConverter.fromScreenX(stepX * resolution)
							+ CoordinateConverter.fromScreenX((stepX + 1) * resolution)) / 2;
					double yc = (CoordinateConverter.fromScreenY(stepY * resolution)
							+ CoordinateConverter.fromScreenY((stepY + 1) * resolution)) / 2;
					Vector fieldVector = defineField(xc, yc);
					double field = Math.log10(fieldVector.norm() + 1);
					heatMapCanvas.setColor(defineColor(field, range));
					heatMapCanvas.fill(
							new Rectangle2D.Double(stepX * resolution, stepY * resolution, resolution, resolution));
//					viewport.drawArrowLine(heatMapCanvas, (int) (stepX + 0.5) * resolution, (int) (stepY + 0.5) * resolution,
//							fieldVector.multiply(0.0005), Color.BLACK, "");
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

	private void defineRange() {
		if (isAdaptiveRange) {
			minValue = Double.MAX_VALUE;
			maxValue = Double.MIN_VALUE;
			range = (Math.max(minField, maxField) - Math.min(minField, maxField)) / 64;
		}
	}

	private Vector defineField(double x, double y) {
		Particle testParticle;
		Vector field = new Vector();
		double distance;
		double dF = 0;
		int pNumber = 0;
		while (Simulation.getInstance().content().particle(pNumber) != null) {
			testParticle = Simulation.getInstance().content().particle(pNumber);
			distance = Functions.defineDistance(testParticle, x, y);
			dF = pairForce.calculatePotential(testParticle, distance);
			field.addToX(dF * (x - testParticle.getX()) / distance);
			field.addToY(dF * (y - testParticle.getY()) / distance);
			pNumber++;
		}
		return field;
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

}
