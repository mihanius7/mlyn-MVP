package elements.line;

import static calculation.Functions.sqr;
import static simulation.Simulation.getInstance;

import calculation.Functions;
import simulation.components.InteractionProcessor;

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
		force = getInstance().interactionProcessor.pairForceType().calculateForce(p1, p2, distance);
		if (Math.abs(force) > maxPairForceCandidate)
			maxPairForceCandidate = Math.abs(force);
		if (getInstance().interactionProcessor.isUsePPCollisions()) {
			if (p1.isCanCollide() && p2.isCanCollide()) {
				if (distance <= p1.getRadius() + p2.getRadius()) {
					force += getInstance().interactionProcessor.collisionForceType().calculateForce(p1, p2, distance);
				}
			}
		}
		Functions.addForce(p2, p1, force, distance);
	}

}