package gui.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class AbstractShape {
	Color color;
 
    abstract public void paintShape(Graphics2D g);
    
    public Color getColor() {
		return color;    	
    }
    
    public void setColor(Color newColor) {
		color = newColor;    	
    }
}
