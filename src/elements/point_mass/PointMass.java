package elements.point_mass;

import elements.Element;
import evaluation.Vector;
import simulation.Simulation;

public class PointMass implements Element, Cloneable {

	protected double m = 1, x = 0, y = 0, lastx, lasty, oldVelocitySmoothed;
	protected Vector vel = new Vector();
	protected Vector lastVel = new Vector();
	protected int movableX, movableY;

	public PointMass() {
		movableX = 1;
		movableY = 1;
	}

	public double getMass() {
		return m;
	}

	public void setMass(double newMass) {
		if (newMass > 0)
			m = newMass;
	}

	public double getX() {
		return x;
	}

	public void setX(double newx) {
		lastx = x;
		x = newx;
	}

	public void addX(double dx) {
		lastx = x;
		x += dx;
	}

	public void setXnoHistory(double newx) {
		x = newx;
	}

	public double getLastX() {
		return lastx;
	}

	public double getY() {
		return y;
	}

	public void setY(double newy) {
		lasty = y;
		y = newy;
	}

	public void addY(double dy) {
		lasty = y;
		y += dy;
	}

	public void setYnoHistory(double newy) {
		y = newy;
	}

	public double getLastY() {
		return lasty;
	}

	public double getVx() {
		return vel.X();
	}

	public double getLastVx() {
		return lastVel.X();
	}

	public void setVx(double newvx) {
		lastVel.setX(vel.X());
		vel.setX(newvx);
	}

	public double getVy() {
		return vel.Y();
	}

	public double getLastVy() {
		return lastVel.Y();
	}

	public void setVy(double newvy) {
		lastVel.setY(vel.Y());
		vel.setY(newvy);
	}

	public void setVelocity(double angle, double magnitude) {
		setVx(magnitude * Math.cos(angle));
		setVy(magnitude * Math.sin(angle));
	}

	public double measureVx() {
		double dt = Simulation.timeStepController.getTimeStepSize();
		return (x - lastx) / dt;
	}

	public double measureVy() {
		double dt = Simulation.timeStepController.getTimeStepSize();
		return (y - lasty) / dt;
	}

	public double measureAx() {
		double dt = Simulation.timeStepController.getTimeStepSize();
		// return (x - 2 * lastx + lastlastx) / dt / dt;
		return (vel.X() - lastVel.X()) / dt;
	}

	public double measureAy() {
		double dt = Simulation.timeStepController.getTimeStepSize();
		// return (y - 2 * lasty + lastlasty) / dt / dt;
		return (vel.Y() - lastVel.Y()) / dt;
	}

	public double defineSquaredVelocity() {
		return vel.normSquared();
	}

	public double defineSquaredVelocitySmoothed() {
		oldVelocitySmoothed -= oldVelocitySmoothed - measureVelocity().normSquared();
		return oldVelocitySmoothed;
	}

	public double defineVelocity() {
		return vel.norm();
	}

	public double defineVelocityAngle() {
		return vel.defineAngle();
	}

	public Vector getVelocityVector() {
		return vel;
	}

	public void setVelocityVector(Vector v) {
		v.setX(v.X());
		v.setY(v.Y());
	}

	public Vector measureVelocity() {
		return new Vector(measureVx(), measureVy());
	}

	public double defineKineticEnergy() {
		return m * defineSquaredVelocity() / 2;
	}

	public double measureKineticEnergy() {
		return m * measureVelocity().normSquared() / 2;
	}

	public void setMovable(boolean b) {
		setMovableX(b);
		setMovableY(b);
	}

	public boolean isMovableX() {
		if (movableX > 0)
			return true;
		else
			return false;
	}

	public void setMovableX(boolean b) {
		if (b == true)
			this.movableX = 1;
		else
			this.movableX = 0;
	}

	public boolean isMovableY() {
		if (movableY > 0)
			return true;
		else
			return false;
	}

	public void setMovableY(boolean b) {
		if (b == true)
			this.movableY = 1;
		else
			this.movableY = 0;
	}

	public boolean isMovable() {
		if (movableX > 0 && movableY > 0)
			return true;
		else
			return false;
	}

	public boolean isMoving() {
		return vel.normSquared() > 1e-15;
	}

}
