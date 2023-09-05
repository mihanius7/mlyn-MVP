package elements.force_pair;

import simulation.Simulation;

public class NeighborPair extends ForcePair {

	public NeighborPair(int i, int j, double currentDistance) {
		super(i, j);
		distance = currentDistance;
		lastDistance = distance;
	}

	public void applyForce() {
		super.applyForce();
		force = Simulation.getInstance().interactionProcessor.applyPairInteraction(p1, p2, distance);
	}

}