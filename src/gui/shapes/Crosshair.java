package gui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import elements.Element;
import gui.viewport.Viewport;

public class Crosshair extends Shape {
	
	public static final Color CROSSHAIR = new Color(128, 128, 128);
	
	private int x;
	private int y;

	@Override
	public void paintShape(Graphics2D g, Viewport viewport) {
		g.setColor(CROSSHAIR);
		g.setStroke(new BasicStroke(2.0f));
		g.drawLine(x, 0, x, viewport.getHeight());
		g.drawLine(0, y, viewport.getWidth(), y);
	}

	@Override
	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
}
