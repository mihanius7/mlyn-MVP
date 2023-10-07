package calculation.pairforce;

import simulation.components.InteractionType;

public class PairForceFactory {
	public static PairForce getCentralForce(InteractionType type) {
		switch (type) {
		case COULOMB:
			return new Coulomb();
		case GRAVITATION:
			return new Gravity();
		case LENNARDJONES:
			return new LennardJones();
		default:
			break;
		}
		return null;
	}
}
