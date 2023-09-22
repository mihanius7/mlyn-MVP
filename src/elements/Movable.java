package elements;

public interface Movable {
	
	public void doMovement();
	
	public double getForceValue();
	
	public double getSafetyReserve();

	public void clearForce();
	
}
