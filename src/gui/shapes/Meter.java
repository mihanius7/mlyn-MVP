package gui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import calculation.Functions;
import elements.Element;
import elements.point.Particle;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Simulation;

public class Meter extends Shape {

	public static final Color METER = new Color(255, 32, 32);

	public static float fontSize = 14;

	private int x1;
	private int x2;
	private int y1;
	private int y2;

	@Override
	public void paintShape(Graphics2D g, Viewport viewport) {
		double screenDistance = Functions.defineDistance(CoordinateConverter.fromScreenX(x1),
				CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y1),
				CoordinateConverter.fromScreenY(y2));
		if (screenDistance > 0) {
			g.setColor(METER);
			g.setStroke(new BasicStroke(1f));
			g.drawLine(x1, y1, x2, y2);
			String label = String.format("%.2e m", screenDistance);
			viewport.drawStringTilted(g, label, x1, y1, x2, y2);
		}
	}
	
	public void refresh() {
		int size = Simulation.getInstance().content().getSelectedParticles().size();
		Particle p1 = Simulation.getInstance().content().getSelectedParticles().get(size - 1);
		Particle p2 = Simulation.getInstance().content().getSelectedParticles().get(size - 2);
		setX1(CoordinateConverter.toScreenX(p1.getX()));
		setY1(CoordinateConverter.toScreenY(p1.getY()));
		setX2(CoordinateConverter.toScreenX(p2.getX()));
		setY2(CoordinateConverter.toScreenY(p2.getY()));
	}

	@Override
	public Element getElement() {
		return null;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

}
