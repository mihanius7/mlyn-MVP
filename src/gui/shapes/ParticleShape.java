package gui.shapes;

import static simulation.Simulation.interactionProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import elements.point_mass.Particle;
import gui.Viewport;

public class ParticleShape extends AbstractShape {
	private Particle p;
	public static final Color PARTICLE_DEFAULT = new Color(100, 100, 100);
	public static final Color PARTICLE_BORDER = Color.DARK_GRAY;
	public static final Color PARTICLE_FIXED = Color.BLACK;
	public static final Color PARTICLE_CROSS = Color.ORANGE;
	public static boolean drawParticleBorders = true;
	public static boolean drawGradientParticles = false;
	public static boolean drawVelocities = false;
	public static boolean drawForces = false;
	public static boolean drawNeighbourRadius = false;
	public static boolean drawTags = false;
	public static BasicStroke particleBorder = new BasicStroke(0.5f);
	private float fontSize = 14;

	public ParticleShape(Particle p) {
		super();
		color = PARTICLE_DEFAULT;
		this.p = p;
	}

	@Override
	public void paintShape(Graphics2D targetG2d) {
		int x = Viewport.toScreenX(p.getX());
		int y = Viewport.toScreenY(p.getY());
		int r = (int) Math.ceil(Viewport.getScale() * p.getRadius());
		if (!drawGradientParticles)
			targetG2d.setPaint(p.getColor());
		else
			targetG2d.setPaint(new GradientPaint(x, y - r, Color.WHITE, x + r, y + r, p.getColor(), false));
		targetG2d.fillOval(x - r, y - r, r * 2, r * 2);
		if (Viewport.isDrawTracks()) {
			int x0 = Viewport.toScreenX(p.getLastX());
			int y0 = Viewport.toScreenY(p.getLastY());
			Viewport.tracksCanvas.setColor(p.getEigeneColor());
			Viewport.tracksCanvas.drawLine(x0, y0, x, y);
		}
		if (drawParticleBorders) {
			targetG2d.setColor(PARTICLE_BORDER);
			targetG2d.setStroke(particleBorder);
			targetG2d.drawOval(x - r, y - r, r * 2, r * 2);
		}
		if (!p.isMovableX()) {
			targetG2d.setColor(PARTICLE_CROSS);
			targetG2d.setStroke(Viewport.crossStroke);
			targetG2d.drawLine(x, y + r + 3, x, y - r - 3);
		}
		if (!p.isMovableY()) {
			targetG2d.setColor(PARTICLE_CROSS);
			targetG2d.setStroke(Viewport.crossStroke);
			targetG2d.drawLine(x - r - 3, y, x + r + 3, y);
		}
		if (drawNeighbourRadius) {
			int nradius = (int) (0.5 * Viewport.getScale() * (interactionProcessor.getNeighborRangeExtra()));
			targetG2d.drawOval(x - nradius, y - nradius, nradius * 2, nradius * 2);
		}
		if (drawForces || p.isSelected())
			Viewport.drawArrowLine(x, y, p.getLastForceVector(), Viewport.ARROW_FORCE, "N");
		if (drawVelocities)
			Viewport.drawArrowLine(x, y, p.getVelocityVector(), Viewport.ARROW_VELOCITY, "m/s");
		if (drawTags || p.isSelected()) {
			float fontSizeCoefficient = (float) (Viewport.getScale() * fontSize / 128.0);
			float stringsInterval = (float) (0.1 * fontSizeCoefficient);
			targetG2d.setFont(Viewport.labelsFont.deriveFont(fontSizeCoefficient));
			targetG2d.setColor(Viewport.FONT_TAGS);
			x = (int) (Viewport.toScreenX(p.getX()) + r * 1);
			y = (int) (Viewport.toScreenY(p.getY()) - r * 2);
			y -= Viewport.labelsFont.getSize() * stringsInterval;
			targetG2d.drawString(String.format("%.1e kg", p.getMass()), x, y);
			y += Viewport.labelsFont.getSize() * stringsInterval;
			targetG2d.drawString(String.format("(%.2f; %.2f) m", p.getX(), p.getY(), p.defineVelocity()), x, y);
			y += Viewport.labelsFont.getSize() * stringsInterval;
			targetG2d.drawString(String.format("%.2f m/s", p.defineVelocity()), x, y);
		}
	}

}
