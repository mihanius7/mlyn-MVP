package gui.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import elements.Element;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;

public class Crosshair extends Shape {
	
	private static final int COORDINATES_MARGIN_PX = 8;

	public static final Color CROSSHAIR = new Color(128, 128, 128);
	
	private int x;
	private int y;

	@Override
	public void paintShape(Graphics2D g, Viewport viewport) {
		g.setColor(CROSSHAIR);
		g.setStroke(new BasicStroke(2.0f));
		g.drawLine(x, 0, x, viewport.getHeight());
		g.drawLine(0, y, viewport.getWidth(), y);
		g.setColor(viewport.getMainFontColor());
		g.drawString(String.format("Ex = %.2e [V/m]", viewport.heatMap.defineField(x, y).X()), x + COORDINATES_MARGIN_PX, y - COORDINATES_MARGIN_PX);
		g.drawString(String.format("%.2f m", CoordinateConverter.fromScreenX(x)), x + COORDINATES_MARGIN_PX, viewport.getHeight() - COORDINATES_MARGIN_PX);
		g.drawString(String.format("%.2f m", CoordinateConverter.fromScreenY(y)), COORDINATES_MARGIN_PX, y - COORDINATES_MARGIN_PX);

	}

	@Override
	public Element getElement() {
		return null;
	}

	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
}
