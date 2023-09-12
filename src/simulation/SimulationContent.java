package simulation;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.kg;

import java.util.Iterator;

import elements.group.ParticleGroup;
import elements.group.SpringGroup;
import elements.line.Spring;
import elements.point.Particle;

public class SimulationContent implements Cloneable {
	
	ParticleGroup particles = new ParticleGroup();
	SpringGroup springs = new SpringGroup();
	Boundaries boundaries = new Boundaries();
	private Particle referenceParticle;
	private final ParticleGroup selectedParticles = new ParticleGroup();
	private final SpringGroup selectedSprings = new SpringGroup();
	
	private int maxSelectedNumber = 1;
	
	public SimulationContent() {
		referenceParticle = new Particle(0, 0, 1 * kg, 6 * cm);
		referenceParticle.getShape().setVisible(false);
	}

	public ParticleGroup getParticles() {
		return particles;
	}
	
	public SpringGroup getSprings() {
		return springs;
	}	

	public Boundaries getBoundaries() {
		return boundaries;
	}
	
	public Particle getParticle(int i) {
		if (i < particles.size() && particles.get(i) != null)
			return particles.get(i);
		else {
			return null;
		}
	}

	public Particle getLastAddedParticle() {
		return particles.get(particles.size() - 1);
	}

	public Particle getReferenceParticle() {
		return referenceParticle;
	}

	public Particle getParticleWithLesserIndex(Particle p1, Particle p2) {
		if (getIndex(p1) > getIndex(p2))
			return p2;
		else
			return p1;
	}

	public Particle getParticleWithLargerIndex(Particle p1, Particle p2) {
		if (getIndex(p1) > getIndex(p2))
			return p1;
		else
			return p2;
	}

	public int getIndex(Particle p) {
		return particles.indexOf((Particle) p);
	}

	public int getIndex(Spring s) {
		return springs.indexOf((Spring) s);
	}

	public Spring getSpring(int i) {
		if (i < springs.size() && springs.get(i) != null)
			return springs.get(i);
		else
			return null;
	}
	
	public void setMaxSelectionNumber(int i) {
		maxSelectedNumber = i;
	}
	
	public void select(Particle p) {
		if (selectedParticles.size() < maxSelectedNumber) {
			selectedParticles.add(p);
			p.select();
		}
	}

	public void select(Spring s) {
		if (selectedSprings.size() < maxSelectedNumber) {
			selectedSprings.add(s);
			s.select();
		}
	}

	public void selectNextSpring() {
		if (selectedSprings.isEmpty())
			select(getSpring(0));
		else {
			selectedSprings.size();
			int lastSelectedIndex = getIndex(selectedSprings.get(0));
			int nextSelectedIndex = lastSelectedIndex + 1;
			if (nextSelectedIndex > getSpringsCount() - 1)
				nextSelectedIndex = 0;
			deselectAll();
			select(getSpring(nextSelectedIndex));
		}
	}

	public void selectPreviousSpring() {
		if (selectedSprings.isEmpty())
			select(getSpring(0));
		else {
			selectedSprings.size();
			int lastSelectedIndex = getIndex(selectedSprings.get(0));
			int nextSelectedIndex = lastSelectedIndex - 1;
			if (nextSelectedIndex < 0)
				nextSelectedIndex = getSpringsCount() - 1;
			deselectAll();
			select(getSpring(nextSelectedIndex));
		}
	}

	public void selectAllParticles() {
		deselectAll();
		Particle p;
		Iterator<Particle> it = particles.iterator();
		while (it.hasNext()) {
			p = it.next();
			select(p);
		}
	}

	public void deselect(Particle p) {
		selectedParticles.remove(p);
		p.deselect();
	}

	public void deselect(Spring s) {
		selectedSprings.remove(s);
		s.deselect();
	}

	public void deselectAll() {
		if (!selectedParticles.isEmpty() || !selectedSprings.isEmpty()) {
			for (Particle p : selectedParticles)
				p.deselect();
			for (Spring s : selectedSprings)
				s.deselect();
			selectedParticles.clear();
			selectedSprings.clear();
		}
	}

	public Particle getSelectedParticle(int index) {
		if (index < selectedParticles.size())
			return selectedParticles.get(index);
		else
			return null;
	}

	public Spring getSelectedSpring(int index) {
		if (index < selectedSprings.size())
			return selectedSprings.get(index);
		else
			return null;
	}
	
	public void removeSelectedParticles() {
		if (!selectedParticles.isEmpty()) {
			Simulation.getInstance().removeParticlesSafety(selectedParticles);
			selectedParticles.clear();
		}
	}

	public void removeSelectedSprings() {
		if (!selectedSprings.isEmpty()) {
			Simulation.getInstance().removeSpringsSafety(selectedSprings);
			selectedSprings.clear();
		}
	}
	
	public ParticleGroup getSelectedParticles() {
		return selectedParticles;
	}

	public SpringGroup getSelectedSprings() {
		return selectedSprings;
	}
	
	public int getParticlesCount() {
		return particles.size();
	}

	public int getSpringsCount() {
		return springs.size();
	}
	
	public Object clone() throws CloneNotSupportedException {
		SimulationContent clone = (SimulationContent) super.clone();
		clone.particles = (ParticleGroup) particles.clone();
		clone.springs = (SpringGroup) springs.clone();
		clone.boundaries = (Boundaries) boundaries.clone();
		return clone;
	}
	
}
