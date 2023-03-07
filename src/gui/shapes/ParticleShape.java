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
	public static final Color PARTICLE_WATCH = Color.ORANGE;
	public static final Color PARTICLE_CROSS = Color.ORANGE;	
	public static boolean drawParticleBorders = true;
	public static boolean drawGradientParticles = false;
	public static boolean drawVelocities = false;
	public static boolean drawForces = false;
	public static boolean drawNeighbourRadius = false;
	public static boolean drawTags = false;	
	public static BasicStroke particleBorder = new BasicStroke(0.5f);
	
	public ParticleShape(Particle p) {
		super();
		this.p = p;
	}

	@Override
	public void paintShape(Graphics2D targetG2d) {
		int x = Viewport.toScreenX(p.getX());
		int y = Viewport.toScreenY(p.getY());
		int r = (int) Math.ceil(Viewport.scale * p.getRadius());
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
			int nradius = (int) (0.5 * Viewport.scale * (interactionProcessor.getNeighborRangeExtra()));
			targetG2d.drawOval(x - nradius, y - nradius, nradius * 2, nradius * 2);
		}
		if (drawForces)
			Viewport.drawArrowLine(targetG2d, x, y, p.getLastForceVector(), Viewport.ARROW_FORCE);
		if (drawVelocities || p.isSelected())
			Viewport.drawArrowLine(targetG2d, x, y, p.getVelocityVector(), Viewport.ARROW_VELOCITY);
		if (drawTags || p.isSelected()) {
			targetG2d.setFont(Viewport.tagFont);
			targetG2d.setColor(Viewport.FONT_TAGS);
			x = (int) (Viewport.toScreenX(p.getX()) + r * 0.707);
			y = (int) (Viewport.toScreenY(p.getY()) - r * 0.707);
			y -= Viewport.tagFont.getSize();
			targetG2d.drawString(String.format("%.1e kg", p.getMass()), x, y);
			y += Viewport.tagFont.getSize();
			targetG2d.drawString(String.format("(%.3f; %.3f) m", p.getX(), p.getY(), p.defineVelocity()), x, y);
			y += Viewport.tagFont.getSize();
			targetG2d.drawString(String.format("%.3f m/s", p.defineVelocity()), x, y);
		}
	}

}
