package gui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import elements.Element;
import gui.Viewport;
import simulation.math.Functions;

public class Rectangle extends Shape {

	public static final Color RECTANGLE = new Color(255, 32, 32);

	private int x1;
	private int x2;
	private int y1;
	private int y2;

	@Override
	public void paintShape(Graphics2D g, Viewport viewport) {
		double screenDistance = Functions.defineDistance(x1, x2, y1, y2);
		if (screenDistance > 0) {
			g.setColor(RECTANGLE);
			g.setStroke(new BasicStroke(2f));
			g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		}
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

	@Override
	public Element getElement() {
		return null;
	}

}
