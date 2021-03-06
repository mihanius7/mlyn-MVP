package elements.groups;

import static java.lang.Math.random;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import elements.point_mass.Particle;
import evaluation.MyMath;
import evaluation.Vector;
import gui.MainWindow;
import gui.Viewport;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Simulation;

public class ParticleGroup extends ArrayList<Particle> implements Cloneable {

	private static final long serialVersionUID = 5565999386880014913L;

	public ParticleGroup() {
		super();
	}

	public ParticleGroup(int number) {
		super(number);
	}

	public double defineMass() {
		double m = 0;
		for (Particle p : this)
			m += p.getMass();
		return m;
	}

	public void setMass(double summMass) {
		double dm = summMass / size();
		for (Particle p : this)
			p.setMass(dm);
	}

	public double defineCharge() {
		double q = 0;
		for (Particle p : this)
			q += p.getCharge();
		return q;
	}

	public void setCharge(double summCharge) {
		double dq = summCharge / size();
		for (Particle p : this)
			p.setCharge(dq);
	}

	public double defineEk() {
		double e = 0;
		for (Particle p : this)
			e += p.defineKineticEnergy();
		return e;
	}

	public void setZeroVelocities() {
		for (Particle p : this)
			p.setVelocity(0, 0);
		MainWindow.println(GUIStrings.VELOCITIES_NULLIFIED);
	}

	public void colorizeByCharge() {
		for (Particle p : this)
			if (p.getCharge() > 0)
				p.setColor(Color.RED);
			else if (p.getCharge() < 0)
				p.setColor(Color.BLUE);
			else if (p.getCharge() == 0)
				p.setColor(ParticleShape.PARTICLE_DEFAULT);
		MainWindow.println(GUIStrings.PARTICLE_COLOURS_CORRESPONDS_TO_CHARGE);
	}

	public void setRandomVelocities(double range) {
		for (Particle p : this)
			p.setVelocity(random() * 2 * Math.PI, range * random());
		MainWindow.println(GUIStrings.PARTICLE_VELOCITIES_RANDOMIZED);
	}

	public Vector defineCenterOfMass() {
		Vector v = MyMath.centreOfMass(toArray(new Particle[] {}));
		Viewport.setCrossX(v.X());
		Viewport.setCrossY(v.Y());
		MainWindow.println("Xc = " + v.X() + ", Yc = " + v.Y());
		return v;
	}

	public void fix() {
		if (!isEmpty()) {
			Particle p;
			Iterator<Particle> it = iterator();
			while (it.hasNext()) {
				p = it.next();
				if (p.isMovable())
					p.setMovable(false);
				else
					p.setMovable(true);
			}
		} else
			MainWindow.NothingIsSelectedMessage();
	}

	public void snapToGrid() {
		if (!isEmpty()) {
			Particle p;
			Iterator<Particle> it = iterator();
			while (it.hasNext()) {
				p = it.next();
				p.snapToGrid();
			}
		} else
			MainWindow.NothingIsSelectedMessage();
	}

	public void setRandomColors() {
		int r, g, b;
		for (Particle p : this) {
			r = (int) Math.round(255 * Math.random());
			g = (int) Math.round(255 * Math.random());
			b = (int) Math.round(255 * Math.random());
			p.setColor(new java.awt.Color(r, g, b));
		}
	}

	@Override
	public String toString() {
		String s = "Size = " + size();
		for (Particle p : this)
			s = s.concat(String.format(String.valueOf(Simulation.getIndex(p)) + "\t%.1e\t%.1e\n", p.getX(), p.getY()));
		return s;
	}
	
	public ParticleGroup clone() {
		ParticleGroup clonedList = new ParticleGroup(size());
		for (int i = 0; i < clonedList.size(); i++) {
			try {
				clonedList.add(get(i).clone());
			} catch (CloneNotSupportedException e) {
				MainWindow.println(GUIStrings.CANT_COPY_A_PARTICLE);
			}	
		}		
		return clonedList;		
	}

}
