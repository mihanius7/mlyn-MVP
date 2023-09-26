package gui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import elements.Element;
import elements.point.Particle;
import gui.Colors;
import gui.CoordinateConverter;
import gui.Viewport;
import simulation.Simulation;

public class ParticleShape extends Shape {

	private Particle p;

	public static final Color PARTICLE_DEFAULT = new Color(100, 100, 100);
	public static final Color PARTICLE_BORDER = Color.DARK_GRAY;
	public static final Color PARTICLE_FIXED = Color.BLACK;
	public static final Color PARTICLE_CROSS = Color.ORANGE;
	public static final Color PARTICLE_SELECTED = Color.YELLOW;

	public static boolean drawParticleBorders = false;
	public static boolean drawGradientParticles = false;
	public static boolean drawVelocities = false;
	public static boolean drawForces = false;
	public static boolean drawNeighbourRadius = false;
	public static boolean drawTags = false;

	public static BasicStroke particleBorder = new BasicStroke(0.5f);

	public ParticleShape(Particle p) {
		super();
		color = PARTICLE_DEFAULT;
		this.p = p;
	}

	@Override
	public void paintShape(Graphics2D targetG2d, Viewport viewport) {
		int x = CoordinateConverter.toScreenX(p.getX());
		int y = CoordinateConverter.toScreenY(p.getY());
		int r = (int) Math.ceil(CoordinateConverter.getScale() * p.getRadius());
		if (!drawGradientParticles)
			targetG2d.setPaint(p.isSelected() ? PARTICLE_SELECTED : color);
		else
			targetG2d.setPaint(new GradientPaint(x, y - r, Color.WHITE, x + r, y + r,
					p.isSelected() ? PARTICLE_SELECTED : color, false));
		targetG2d.fillOval(x - r, y - r, r * 2, r * 2);
		if (viewport.isDrawTracks()) {
			int x0 = CoordinateConverter.toScreenX(p.getLastX());
			int y0 = CoordinateConverter.toScreenY(p.getLastY());
			viewport.tracksCanvas.setColor(color);
			viewport.tracksCanvas.drawLine(x0, y0, x, y);
		}
		if (drawParticleBorders) {
			targetG2d.setColor(PARTICLE_BORDER);
			targetG2d.setStroke(particleBorder);
			targetG2d.drawOval(x - r, y - r, r * 2, r * 2);
		}
		if (p.isMovableX() == 0) {
			targetG2d.setColor(PARTICLE_CROSS);
			targetG2d.setStroke(viewport.crossStroke);
			targetG2d.drawLine(x, y + r + 3, x, y - r - 3);
		}
		if (p.isMovableY() == 0) {
			targetG2d.setColor(PARTICLE_CROSS);
			targetG2d.setStroke(viewport.crossStroke);
			targetG2d.drawLine(x - r - 3, y, x + r + 3, y);
		}
		if (drawNeighbourRadius) {
			int nradius = (int) (0.5 * viewport.getScale()
					* (Simulation.getInstance().interactionProcessor.getNeighborRangeExtra()));
			targetG2d.drawOval(x - nradius, y - nradius, nradius * 2, nradius * 2);
		}
		if (drawForces || p.isSelected())
			viewport.drawArrowLine(targetG2d, x, y, p.getLastForceVector(), Colors.ARROW_FORCE, "N");
		if (drawVelocities)
			viewport.drawArrowLine(targetG2d, x, y, p.getVelocityVector(), Colors.ARROW_VELOCITY, "m/s");
		if (drawTags || p.isSelected()) {
			targetG2d.setFont(viewport.labelsFont.deriveFont(viewport.getCurrentFontSize()));
			targetG2d.setColor(Colors.FONT_TAGS);
			float stringsInterval = (float) (0.08 * viewport.getCurrentFontSize());
			targetG2d.setFont(viewport.labelsFont.deriveFont(viewport.getCurrentFontSize()));
			targetG2d.setColor(Colors.FONT_TAGS);
			x = (int) (CoordinateConverter.toScreenX(p.getX()) + r * 1);
			y = (int) (CoordinateConverter.toScreenY(p.getY()) - r * 2);
			y -= viewport.labelsFont.getSize() * stringsInterval;
			targetG2d.drawString(String.format("%.1f kg, %.1e C", p.getMass(), p.getCharge()), x, y);
			y += viewport.labelsFont.getSize() * stringsInterval;
			targetG2d.drawString(String.format("(%.2f; %.2f) m", p.getX(), p.getY(), p.defineVelocity()), x, y);
			y += viewport.labelsFont.getSize() * stringsInterval;
			targetG2d.drawString(String.format("%.2f m/s", p.defineVelocity()), x, y);
		}
	}

	@Override
	public Element getElement() {
		return p;
	}

}
