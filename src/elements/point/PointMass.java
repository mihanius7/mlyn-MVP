package elements.point;

import elements.Movable;
import simulation.Simulation;
import simulation.math.TrajectoryIntegrator;
import simulation.math.Vector;

public class PointMass implements Cloneable, Movable {

	protected double m;
	protected double x, y, lastx, lasty;
	protected double oldVelocitySmoothed;
	protected Vector velocity = new Vector();
	protected Vector lastVelocity = new Vector();
	protected Vector force = new Vector();
	protected Vector lastForce = new Vector();
	protected double frictionForce, stictionForce;
	protected byte movableX = 1, movableY = 1;
	
	protected static TrajectoryIntegrator integrator;

	public PointMass(double x, double y, double m) {
		this.x = x;
		this.y = y;
		this.setMass(m);
		integrator = new TrajectoryIntegrator();
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
		double dt = Simulation.getInstance().timeStepController.getTimeStepSize();
		return (x - lastx) / dt;
	}

	public double measureVy() {
		double dt = Simulation.getInstance().timeStepController.getTimeStepSize();
		return (y - lasty) / dt;
	}

	public double measureAx() {
		double dt = Simulation.getInstance().timeStepController.getTimeStepSize();
		return (velocity.X() - lastVelocity.X()) / dt;
	}

	public double measureAy() {
		double dt = Simulation.getInstance().timeStepController.getTimeStepSize();
		return (velocity.Y() - lastVelocity.Y()) / dt;
	}

	public double getFx() {
		return force.X();
	}

	public double getFy() {
		return force.Y();
	}

	public Vector getForceVector() {
		return force;
	}

	public Vector getLastForceVector() {
		return lastForce;
	}

	public void setFx(double newFx) {
		lastForce.setX(force.X());
		force.setX(newFx);
	}

	public void setFy(double newFy) {
		lastForce.setY(force.Y());
		force.setY(newFy);
	}

	public void addFx(double dfx) {
		force.addToX(dfx);
	}

	public void addFy(double dfy) {
		force.addToY(dfy);
	}

	public void addVx(double dvx) {
		lastVelocity.setX(velocity.X());
		velocity.addToX(dvx);
	}

	public void addVy(double dvy) {
		lastVelocity.setY(velocity.Y());
		velocity.addToY(dvy);
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
		velocity.setX(v.X());
		velocity.setY(v.Y());
	}

	public Vector getLastVelocityVector() {
		return lastVelocity;
	}

	public void setLastVelocityVector(Vector v) {
		lastVelocity.setX(v.X());
		lastVelocity.setY(v.Y());
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

	@Override
	public void doMovement() {
		integrator.calculateNextVelocity(this);
		integrator.calculateNextLocation(this);
	}

	public void clearForce() {
		lastForce.setXY(force.X(), force.Y());
		force.setXY(0, 0);
	}

	public void clearForcesAndHistory() {
		lastForce.setXY(0, 0);
		force.setXY(0, 0);
	}

	public void setMovable(boolean b) {
		setMovableX(b);
		setMovableY(b);
		if (!b)
			velocity.setXY(0, 0);
	}

	public byte isMovableX() {
		return (byte) (movableX > 0 ? 1 : 0);
	}

	public void setMovableX(boolean b) {
		this.movableX = (byte) (b ? 1 : 0);
	}

	public byte isMovableY() {
		return (byte) (movableY > 0 ? 1 : 0);
	}

	public void setMovableY(boolean b) {
		this.movableY = (byte) (b ? 1 : 0);
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
	
	public double getFrictionForce() {
		return frictionForce;
	}

	public void setFrictionForce(double fr) {
		this.stictionForce = (fr < 0) ? 0 : fr;
	}

	public double getStictionForce() {
		return stictionForce;
	}

	public void setStictionForce(double sff) {
		this.stictionForce = (sff < 0) ? 0 : sff;
	}
	
	public boolean isStictionReached() {
		return lastForce.normSquared() > stictionForce * stictionForce;
	}

	@Override
	public double getForceValue() {
		return 0;
	}

	@Override
	public double getSafetyReserve() {
		return Double.MAX_VALUE;
	}

}
