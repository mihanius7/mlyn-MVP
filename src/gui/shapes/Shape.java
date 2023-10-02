package gui.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import elements.Element;
import gui.viewport.Viewport;

public abstract class Shape {
	Color color;
	boolean visible = true;
 
    abstract public void paintShape(Graphics2D g, Viewport viewport);
    
    public Color getColor() {
		return color;    	
    }
    
    public void setColor(Color newColor) {
		color = newColor;    	
    }

	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean v) {
		this.visible= v;
	}
	
	abstract public Element getElement();
}
