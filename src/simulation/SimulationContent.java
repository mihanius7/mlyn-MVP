package simulation;

import elements.groups.ParticleGroup;
import elements.groups.SpringGroup;
import simulation.components.Boundaries;

public class SimulationContent implements Cloneable {
	
	ParticleGroup particles = new ParticleGroup();
	SpringGroup springs = new SpringGroup();
	Boundaries boundaries = new Boundaries();

	public ParticleGroup getParticles() {
		return particles;
	}
	
	public SpringGroup getSprings() {
		return springs;
	}	

	public Boundaries getBoundaries() {
		return boundaries;
	}	
	
	public Object clone() throws CloneNotSupportedException {
		SimulationContent clone = (SimulationContent) super.clone();
		clone.particles = (ParticleGroup) particles.clone();
		clone.springs = (SpringGroup) springs.clone();
		clone.boundaries = (Boundaries) boundaries.clone();
		return clone;
	}
	
}
