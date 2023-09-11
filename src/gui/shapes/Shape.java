package gui.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import gui.Viewport;

public abstract class Shape {
	Color color;
 
    abstract public void paintShape(Graphics2D g, Viewport viewport);
    
    public Color getColor() {
		return color;    	
    }
    
    public void setColor(Color newColor) {
		color = newColor;    	
    }
}
