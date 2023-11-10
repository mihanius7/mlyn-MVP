package gui.shapes;

import static calculation.constants.PhysicalConstants.cm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import calculation.constants.PhysicalConstants;
import elements.Element;
import elements.line.Spring;
import elements.point.Particle;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;

public class SpringShape extends Shape {
	
	private Spring s;
	
	public static final Color SPRING_DEFAULT = new Color(80, 80, 80);
	public static final Color SPRING_OFF = new Color(230, 200, 200);
	public static final Color SPRING_SELECTED = Color.YELLOW;
	
	private boolean drawLabel;
	
	public static float fontSize = 14;
	public static final float SPRING_ZIGZAG_AMPLITUDE = 0.06f;
	private static final double SPRING_ZIGZAG_PERIOD = 4 * cm;

	public SpringShape(Spring s) {
		super();
		color = SPRING_DEFAULT;
		this.s = s;
	}

	@Override
	public void paintShape(Graphics2D targetG2d, Viewport viewport) {
		Particle p1 = s.getFirstParticle();
		Particle p2 = s.getSecondParticle();
		int x1 = CoordinateConverter.toScreenX(p1.getX());
		int y1 = CoordinateConverter.toScreenY(p1.getY());
		int x2 = CoordinateConverter.toScreenX(p2.getX());
		int y2 = CoordinateConverter.toScreenY(p2.getY());
		targetG2d.setColor(s.isSelected()? SPRING_SELECTED : color);
		targetG2d.setStroke(new BasicStroke((float) (viewport.getScale() * s.getVisibleWidth()), BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_BEVEL));
		if (s.isLine())
			targetG2d.drawLine(x1, y1, x2, y2);
		else {
			drawZigzag(targetG2d, x1, y1, s.getNominalLength(), s.defineAngle());
		}
		if (drawLabel || s.isSelected()) {
			String label = String.format("%.3f kgf", s.getForceSmoothed() / PhysicalConstants.kgf);
			viewport.drawStringTilted(targetG2d, label, x1, y1, x2, y2);
		}
	}

	private void drawZigzag(Graphics2D targetG2d, int x1, int y1, double length, double alpha) {
		double beta = alpha + Math.PI / 2;
		double n = length / SPRING_ZIGZAG_PERIOD;
		double step = CoordinateConverter.toScreen(s.getDeformatedLength()) / n;
		double width = CoordinateConverter.toScreen(SPRING_ZIGZAG_AMPLITUDE);
		targetG2d.translate(x1, y1);
		targetG2d.rotate(beta);
		int b = 1;
		targetG2d.draw(new Line2D.Double(0, 0, step, width));
		for (int i = 1; i < n - 2; i++) {
			targetG2d.draw(new Line2D.Double(i * step, b * width, (i + 1) * step, -b * width));
			b *= -1;
		}
		targetG2d.draw(new Line2D.Double((n - 1) * step - step / 1.75, b * width, n * step, 0));
		targetG2d.rotate(-beta);
		targetG2d.translate(-x1, -y1);
	}


	public boolean isDrawLabel() {
		return drawLabel;
	}

	public void setDrawLabel(boolean b) {
		this.drawLabel = b;
	}
	
	@Override
	public Element getElement() {
		return s;
	}

}
