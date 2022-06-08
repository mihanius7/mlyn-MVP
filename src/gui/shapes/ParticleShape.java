package gui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import elements.point_mass.Particle;
import gui.Viewport;

public class ParticleShape extends AbstractShape {
	
	private Particle p;
	private BasicStroke particleBorder = new BasicStroke(0.25f);
	private static boolean drawParticleBorders = true;
	private static final Color PARTICLE_BORDER = Color.DARK_GRAY;
	public static boolean drawGradientParticles = false;
	
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
	}

}
