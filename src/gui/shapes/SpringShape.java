package gui.shapes;

import static constants.PhysicalConstants.cm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import constants.PhysicalConstants;
import elements.force_pair.Spring;
import elements.point_mass.Particle;
import gui.Viewport;

public class SpringShape extends AbstractShape {
	private Spring s;
	private boolean drawTag;
	private float fontSize = 14;
	public static final Color SPRING_DEFAULT = new Color(80, 80, 80);
	public static final Color SPRING_OFF = new Color(230, 200, 200);
	public static final float SPRING_ZIGZAG_AMPLITUDE = 0.06f;

	public SpringShape(Spring s) {
		super();
		color = SPRING_DEFAULT;
		this.s = s;
	}

	@Override
	public void paintShape(Graphics2D targetG2d) {
		Particle p1 = s.getFirstParticle();
		Particle p2 = s.getSecondParticle();
		int x1 = Viewport.toScreenX(p1.getX());
		int y1 = Viewport.toScreenY(p1.getY());
		int x2 = Viewport.toScreenX(p2.getX());
		int y2 = Viewport.toScreenY(p2.getY());
		targetG2d.setColor(s.getColor());
		targetG2d.setStroke(new BasicStroke((float) (Viewport.getScale() * s.getVisibleWidth()), BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_BEVEL));
		if (s.isLine())
			targetG2d.drawLine(x1, y1, x2, y2);
		else {
			double alpha = s.defineAngle();
			double beta = alpha + Math.PI / 2;
			double n = s.getNominalLength() / (4 * cm);
			double step = Viewport.toScreen(s.getDeformatedLength()) / n;
			double width = Viewport.toScreen(SPRING_ZIGZAG_AMPLITUDE);
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
		if (drawTag || s.isSelected()) {
			drawTag(targetG2d, x1, y1, x2, y2);
		}
	}

	private void drawTag(Graphics2D targetG2d, int x1, int y1, int x2, int y2) {
		double alpha = s.defineAngle() + Math.PI / 2;
		double scale = Viewport.getScale();
		targetG2d.setFont(Viewport.tagFont.deriveFont((float) (scale * fontSize / 128.0)));
		targetG2d.setColor(Viewport.FONT_TAGS);
		int xc = Math.min(x1, x2) + (Math.max(x1, x2) - Math.min(x1, x2)) / 2;
		int yc = Math.min(y1, y2) + (Math.max(y1, y2) - Math.min(y1, y2)) / 2;
		alpha = evaluation.MyMath.normalizeAbsAngle(alpha);
		targetG2d.translate(xc, yc);
		targetG2d.rotate(alpha);
		targetG2d.drawString(String.format("%.3f kg", s.getForceSmoothed() / PhysicalConstants.kgf), (int) -(scale * fontSize / 50.0), (int) -(scale * fontSize / 256.0));
		targetG2d.rotate(-alpha);
		targetG2d.translate(-xc, -yc);
	}

	public boolean isDrawTag() {
		return drawTag;
	}

	public void setDrawTag(boolean drawTag) {
		this.drawTag = drawTag;
	}

}
