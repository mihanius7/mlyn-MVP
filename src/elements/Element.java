package elements;

import java.awt.geom.Point2D;

public interface Element {
	
	public Point2D.Double getCenterPoint();	

	public boolean isCanCollide();

	public void setCanCollide(boolean b);
	
	public void select();

	public void deselect();
	
	public boolean isSelected();
}
