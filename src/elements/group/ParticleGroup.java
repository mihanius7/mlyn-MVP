package elements.group;

import static java.lang.Math.random;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import elements.point.Particle;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Simulation;
import simulation.math.Functions;
import simulation.math.Vector;

public class ParticleGroup extends ArrayList<Particle> implements Cloneable {

	private static final long serialVersionUID = 5565999386880014913L;

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
		ConsoleWindow.println(GUIStrings.VELOCITIES_NULLIFIED);
	}

	public void colorizeByCharge() {
		for (Particle p : this)
			if (p.getCharge() > 0)
				p.getShape().setColor(Color.RED);
			else if (p.getCharge() < 0)
				p.getShape().setColor(Color.BLUE);
			else if (p.getCharge() == 0)
				p.getShape().setColor(ParticleShape.PARTICLE_DEFAULT);
		ConsoleWindow.println(GUIStrings.PARTICLE_COLOURS_CORRESPONDS_TO_CHARGE);
	}

	public void setRandomVelocities(double range) {
		for (Particle p : this)
			p.setVelocity(random() * 2 * Math.PI, range * random());
		ConsoleWindow.println(GUIStrings.PARTICLE_VELOCITIES_RANDOMIZED);
	}

	public Vector defineCenterOfMass() {
		Vector v = Functions.centreOfMass(toArray(new Particle[] {}));
		ConsoleWindow.println("Xc = " + v.X() + ", Yc = " + v.Y());
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

	public void snapToGrid(double gridSize) {
		if (!isEmpty()) {
			Particle p;
			Iterator<Particle> it = iterator();
			while (it.hasNext()) {
				p = it.next();
				p.snapToGrid(gridSize);
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
			p.getShape().setColor(new java.awt.Color(r, g, b));
		}
	}
	
	public Particle findNearestParticle(double x, double y, double maxDistance) {
		double minSqDist = Double.MAX_VALUE, sqDist;
		Particle nearest = null;
		for (Particle p : this) {
			sqDist = Functions.defineSquaredDistance(p, x, y) - Functions.sqr(p.getRadius());
			if (sqDist < minSqDist) {
				minSqDist = sqDist;
				nearest = p;
			}
		}
		if (minSqDist > Functions.sqr(maxDistance))
			nearest = null;
		return nearest;
	}

	@Override
	public String toString() {
		String s = "Size = " + size();
		for (Particle p : this)
			s = s.concat(String.format(String.valueOf(Simulation.getInstance().getContent().getIndex(p)) + "\t%.1e\t%.1e\n", p.getX(), p.getY()));
		return s;
	}
	
	public ParticleGroup clone() {
		ParticleGroup clonedList = new ParticleGroup();
		for (int i = 0; i < clonedList.size(); i++) {
			try {
				clonedList.add(get(i).clone());
			} catch (CloneNotSupportedException e) {
				ConsoleWindow.println(GUIStrings.CANT_COPY_A_PARTICLE);
			}	
		}		
		return clonedList;		
	}
	
	public void selectInRect(double x1, double y1, double x2, double y2) {
		Rectangle2D.Double rect = new Rectangle2D.Double(Math.min(x2, x1), Math.min(y2, y1), Math.abs(x2 - x1), Math.abs(y2 - y1));
		for (Particle p : this) {
			if (rect.contains(p.getCenterPoint())) {
				Simulation.getInstance().getContent().select(p);
			} else {
				Simulation.getInstance().getContent().deselect(p);
			}
		};	
	}

}
