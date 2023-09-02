package elements.point_mass;

import java.awt.geom.Point2D;

import elements.Element;
import evaluation.Vector;
import simulation.Simulation;

public class PointMass implements Element, Cloneable  {

	protected double m;
	protected double x, y, lastx, lasty;
	protected double oldVelocitySmoothed;
	protected Vector velocity = new Vector();
	protected Vector lastVelocity = new Vector();
	protected int movableX = 1, movableY = 1;

	public PointMass(double x, double y, double m) {
		this.x = x;
		this.y = y;
		this.setMass(m);
	}

	public double getMass() {
		return m;
	}

	public void setMass(double newMass) {
		if (newMass > 0)
			m = newMass;
		else
			throw new RuntimeException("Can't set particle mass to zero. ");
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
		return velocity.X();
	}

	public double getLastVx() {
		return lastVelocity.X();
	}

	public void setVx(double newvx) {
		lastVelocity.setX(velocity.X());
		velocity.setX(newvx);
	}

	public double getVy() {
		return velocity.Y();
	}

	public double getLastVy() {
		return lastVelocity.Y();
	}

	public void setVy(double newvy) {
		lastVelocity.setY(velocity.Y());
		velocity.setY(newvy);
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
		return (velocity.X() - lastVelocity.X()) / dt;
	}

	public double measureAy() {
		double dt = Simulation.timeStepController.getTimeStepSize();
		return (velocity.Y() - lastVelocity.Y()) / dt;
	}

	public double defineSquaredVelocity() {
		return velocity.normSquared();
	}

	public double defineSquaredVelocitySmoothed() {
		oldVelocitySmoothed -= oldVelocitySmoothed - measureVelocity().normSquared();
		return oldVelocitySmoothed;
	}

	public double defineVelocity() {
		return velocity.norm();
	}

	public double defineVelocityAngle() {
		return velocity.defineAngle();
	}

	public Vector getVelocityVector() {
		return velocity;
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
		if (!b)
			velocity.setXY(0, 0);
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
		return velocity.normSquared() > 1e-15;
	}

	@Override
	public java.awt.geom.Point2D.Double getCenterPoint() {
		return new Point2D.Double(x, y);
	}

}
