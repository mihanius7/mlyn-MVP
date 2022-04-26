package elements.point_mass;

import java.awt.Color;

import simulation.Simulation;
import simulation.components.Boundaries;
import elements.Interactable;
import elements.Selectable;
import evaluation.MyMath;
import evaluation.Vector;
import gui.Viewport;

public class Particle extends PointMass implements Cloneable, Selectable, Interactable {

	protected double r = 0.1, q = 0;
	protected Vector force = new Vector();
	protected Vector lastForce = new Vector();
	protected boolean visible = true, canCollide = true, isSelected = false;
	protected double frictionForce = 0, stictionForce = 0;
	protected double elasticity = 0.995;
	protected Color oldColor, color = Viewport.PARTICLE_DEFAULT;

	public Particle(double x, double y, double m, double q, double vx, double vy, double radius, Color c) {
		this.x = x;
		this.y = y;
		this.q = q;
		this.m = m;
		vel.setX(vx);
		vel.setY(vy);
		this.r = radius;
		this.color = c;
		lastx = x;
		lasty = y;
		lastVel.setXY(vel.X(), vel.Y());
	}

	public Particle(double x, double y, double m, double radius) {
		this.x = x;
		this.y = y;
		this.m = m;
		this.r = radius;
		lastx = x;
		lasty = y;
	}

	public Particle(double x, double y, Particle referenceParticle) {
		this.x = x;
		this.y = y;
		this.m = referenceParticle.getMass();
		this.r = referenceParticle.getRadius();
		this.q = referenceParticle.getCharge();
		this.color = referenceParticle.getColor();
		this.vel.setXY(referenceParticle.getVx(), referenceParticle.getVy());
		this.movableX = (referenceParticle.isMovableX() == true) ? 1 : 0;
		this.movableY = (referenceParticle.isMovableY() == true) ? 1 : 0;
		this.elasticity = referenceParticle.getElasticity();
		lastx = x;
		lasty = y;
		lastVel.setXY(vel.X(), vel.Y());
	}

	public Particle(double x, double y, double vx, double vy, Particle referenceParticle) {
		this.x = x;
		this.y = y;
		vel.setX(vx);
		vel.setY(vy);
		this.m = referenceParticle.getMass();
		this.r = referenceParticle.getRadius();
		this.q = referenceParticle.getCharge();
		this.color = referenceParticle.getColor();
		this.movableX = (referenceParticle.isMovableX() == true) ? 1 : 0;
		this.movableY = (referenceParticle.isMovableY() == true) ? 1 : 0;
		this.elasticity = referenceParticle.getElasticity();
		lastx = x;
		lasty = y;
		lastVel.setXY(vel.X(), vel.Y());
	}

	public Particle(double x, double y, double vx, double vy, double m, double radius) {
		this.x = x;
		this.y = y;
		this.m = m;
		vel.setX(vx);
		vel.setY(vy);
		this.r = radius;
		lastx = x;
		lasty = y;
		lastVel.setXY(vel.X(), vel.Y());
	}

	public void applyNewVelocity(double dt, boolean useFriction) {
		lastVel.setX(vel.X());
		lastVel.setY(vel.Y());
		if (Simulation.interactionProcessor.isUseExternalForces()) {
			force.addToX(Simulation.interactionProcessor.getExternalAccelerationX() * m);
			force.addToY(Simulation.interactionProcessor.getExternalAccelerationY() * m);
		}
		if (isMoving()) {
			if (useFriction) {
				double airFriction = Simulation.interactionProcessor.getAirFrictionCoefficient();
				force.addToXY(-vel.X() * airFriction, -vel.Y() * airFriction);
				double ffx = -vel.defineCos() * frictionForce;
				double ffy = -vel.defineSin() * frictionForce;
				force.addToXY(ffx, ffy);
			}
			velocityVerlet(dt);
		} else {
			if (useFriction && isStictionReached())
				velocityVerlet(dt);
			else
				velocityVerlet(dt);
		}
	}

	private void velocityVerlet(double dt) {
		vel.addToX(movableX * dt * (lastForce.X() + force.X()) / (2 * m));
		vel.addToY(movableY * dt * (lastForce.Y() + force.Y()) / (2 * m));
	}

	public void move(double dt) {
		lastx = x;
		x += movableX * vel.X() * dt + movableX * (force.X() * dt * dt) / (2 * m);
		lasty = y;
		y += movableY * vel.Y() * dt + movableY * (force.Y() * dt * dt) / (2 * m);
	}

	public void clearForce() {
		lastForce.setXY(force.X(), force.Y());
		force.setXY(0, 0);
	}

	public void clearForcesAndHistory() {
		lastForce.setXY(0, 0);
		force.setXY(0, 0);
	}

	public void applyBoundaryConditions() {

		Boundaries b = Simulation.getContent().getBoundaries();

		if (b.isUseRight() && x + r > b.getRight()) {
			vel.multiplyXby(-elasticity);
			setX(b.getRight() - r);
			vel.multiplyYby(0.95);
		} else if (b.isUseLeft() && x - r < b.getLeft()) {
			vel.multiplyXby(-elasticity);
			setX(b.getLeft() + r);
			vel.multiplyYby(0.95);
		}

		if (b.isUseBottom() && b.getBottom() > y - r) {
			double newvy = -vel.Y() * elasticity;
			if (vel.Y() < -1E-6)
				setVy(newvy);
			else
				setVy(0);
			setY(b.getBottom() + r);
			vel.multiplyXby(0.95);
		} else if (b.isUseUpper() && y + r > b.getUpper()) {
			vel.multiplyYby(-elasticity);
			setY(b.getUpper() - r);
			vel.multiplyXby(0.95);
		}
	}

	public double getCharge() {
		return q;
	}

	public void setCharge(double q) {
		this.q = q;
	}

	public double getRadius() {
		return r;
	}

	public void setRadius(double newRadius) {
		if (newRadius > 0)
			r = newRadius;
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
		lastVel.setX(vel.X());
		vel.addToX(dvx);
	}

	public void addVy(double dvy) {
		lastVel.setY(vel.Y());
		vel.addToY(dvy);
	}

	public boolean isStictionReached() {
		return lastForce.normSquared() > stictionForce * stictionForce;
	}

	public Color getColor() {
		if (!isSelected)
			return color;
		else
			return Viewport.SELECTED;
	}

	public Color getEigeneColor() {
		return color;
	}

	public void setColor(Color newColor) {
		oldColor = color;
		color = newColor;
	}

	public void setOldColor() {
		color = oldColor;
	}

	public void snapToGrid() {
		x = MyMath.roundTo(x, 1 / Viewport.getGridSize());
		y = MyMath.roundTo(y, 1 / Viewport.getGridSize());
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean v) {
		this.visible = v;
	}

	public double getFrictionForce() {
		return frictionForce;
	}

	public void setFrictionForce(double fr) {
		if (fr < 0)
			fr = 0;
		frictionForce = fr;
	}

	public double getStictionForce() {
		return stictionForce;
	}

	public void setStictionForce(double sff) {
		if (sff < 0)
			sff = 0;
		this.stictionForce = sff;
	}

	public double getElasticity() {
		return elasticity;
	}

	public void setElasticity(double e) {
		if (e > 1)
			e = 1;
		else if (e < 0)
			e = 0;
		this.elasticity = e;
	}

	public Particle clone() throws CloneNotSupportedException {
		Particle clone = (Particle) super.clone();
		clone.vel = (vel.clone());
		clone.lastVel = (lastVel.clone());
		clone.force = (force.clone());
		clone.lastForce = (lastForce.clone());
		return (Particle) clone;
	}
	
	@Override
	public void select() {
		isSelected = true;
	}

	@Override
	public void deselect() {
		isSelected = false;
	}

	public boolean isSelected() {
		return isSelected;
	}
	
	public boolean isCanCollide() {
		return canCollide;
	}

	public void setCanCollide(boolean canCollide) {
		this.canCollide = canCollide;
	}

}