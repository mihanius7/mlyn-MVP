package elements.line;

import simulation.Simulation;

public class NeighborPair extends Pair {
	
	public static double maxPairForce;
	public static double maxPairForceCandidate;

	public NeighborPair(int i, int j, double currentDistance) {
		super(i, j);
		distance = currentDistance;
		lastDistance = distance;
	}
	
	@Override
	public void doForce() {
		super.doForce();
		force = Simulation.getInstance().interactionProcessor.applyPairInteraction(p1, p2, distance);
		if (Math.abs(force) > maxPairForceCandidate)
			maxPairForceCandidate = Math.abs(force);
	}

}