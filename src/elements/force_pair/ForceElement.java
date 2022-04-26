package elements.force_pair;

import elements.Element;

public interface ForceElement extends Element {
	public void applyForce();

	public double getForce();

	public double getReserveRatio();
}
