package elements.line;

import simulation.Simulation;

public class NeighborPair extends Pair {

	public NeighborPair(int i, int j, double currentDistance) {
		super(i, j);
		distance = currentDistance;
		lastDistance = distance;
	}
	
	@Override
	public void doMovement() {
		super.doMovement();
		force = Simulation.getInstance().interactionProcessor.applyPairInteraction(p1, p2, distance);
	}

}