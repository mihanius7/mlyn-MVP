package elements.force_pair;

import static simulation.Simulation.interactionProcessor;

public class NeighborPair extends ForcePair {

	public NeighborPair(int i, int j, double currentDistance) {
		super(i, j);
		distance = currentDistance;
		lastDistance = distance;
	}

	public void applyForce() {
		super.applyForce();
		force = interactionProcessor.applyPairInteraction(p1, p2, distance);
	}

}