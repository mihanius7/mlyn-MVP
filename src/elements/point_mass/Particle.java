package elements.point_mass;

import java.awt.Color;

import simulation.Simulation;
import simulation.components.Boundaries;
import elements.Interactable;
import elements.Selectable;
import evaluation.MyMath;
import evaluation.Vector;
import gui.Viewport;
import gui.shapes.ParticleShape;

public class Particle extends PointMass implements Cloneable, Selectable, Interactable {

	protected double r, q;
	protected Vector force = new Vector();
	protected Vector lastForce = new Vector();
	protected boolean visible = true, canCollide = true, isSelected = false;
	protected double frictionForce = 0, stictionForce = 0;
	protected double elasticity = 0.995;
	protected Color oldColor, color;
	protected ParticleShape shape;

	public Particle(double x, double y, double m, double q, double vx, double vy, double radius, Color c) {
		this.x = x;
		this.y = y;
		this.q = q;
		this.m = m;
		velocity.setX(vx);
		velocity.setY(vy);
		this.r = radius;
		this.color = c;
		lastx = x;
		lasty = y;
		lastVelocity.setXY(velocity.X(), velocity.Y());
		shape = new ParticleShape(this);
	}
	
	public Particle(double x, double y, double vx, double vy, double m, double radius) {
		this(x, y, m, vx, vy, 0, radius, Viewport.PARTICLE_DEFAULT);
	}

	public Particle(double x, double y, double m, double radius) {
		this(x, y, m, 0, 0, 0, radius, Viewport.PARTICLE_DEFAULT);
	}

	public Particle(double x, double y, Particle referenceParticle) {
		this(x, y, referenceParticle.getMass(), referenceParticle.getCharge(), referenceParticle.getVx(),
				referenceParticle.getVy(), referenceParticle.getRadius(), referenceParticle.getColor());
		this.movableX = (referenceParticle.isMovableX() == true) ? 1 : 0;
		this.movableY = (referenceParticle.isMovableY() == true) ? 1 : 0;
		this.elasticity = referenceParticle.getElasticity();
	}

	public void applyNewVelocity(double dt, boolean useFriction) {
		lastVelocity.setX(velocity.X());
		lastVelocity.setY(velocity.Y());
		if (Simulation.interactionProcessor.isUseExternalForces()) {
			force.addToX(Simulation.interactionProcessor.getExternalAccelerationX() * m);
			force.addToY(Simulation.interactionProcessor.getExternalAccelerationY() * m);
		}
		if (isMoving()) {
			if (useFriction) {
				double airFriction = Simulation.interactionProcessor.getAirFrictionCoefficient();
				force.addToXY(-velocity.X() * airFriction, -velocity.Y() * airFriction);
				double ffx = -velocity.defineCos() * frictionForce;
				double ffy = -velocity.defineSin() * frictionForce;
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
		velocity.addToX(movableX * (dt * (lastForce.X() + force.X()) / (2 * m)));
		velocity.addToY(movableY * (dt * (lastForce.Y() + force.Y()) / (2 * m)));
	}

	public void move(double dt) {
		lastx = x;
		x += movableX * velocity.X() * dt + movableX * (force.X() * dt * dt) / (2 * m);
		lasty = y;
		y += movableY * velocity.Y() * dt + movableY * (force.Y() * dt * dt) / (2 * m);
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
			velocity.multiplyXby(-elasticity);
			setX(b.getRight() - r);
			velocity.multiplyYby(0.95);
		} else if (b.isUseLeft() && x - r < b.getLeft()) {
			velocity.multiplyXby(-elasticity);
			setX(b.getLeft() + r);
			velocity.multiplyYby(0.95);
		}

		if (b.isUseBottom() && b.getBottom() > y - r) {
			double newvy = -velocity.Y() * elasticity;
			if (velocity.Y() < -1E-6)
				setVy(newvy);
			else
				setVy(0);
			setY(b.getBottom() + r);
			velocity.multiplyXby(0.95);
		} else if (b.isUseUpper() && y + r > b.getUpper()) {
			velocity.multiplyYby(-elasticity);
			setY(b.getUpper() - r);
			velocity.multiplyXby(0.95);
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
		lastVelocity.setX(velocity.X());
		velocity.addToX(dvx);
	}

	public void addVy(double dvy) {
		lastVelocity.setY(velocity.Y());
		velocity.addToY(dvy);
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
		clone.velocity = (velocity.clone());
		clone.lastVelocity = (lastVelocity.clone());
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
	
	public ParticleShape getShape() {
		return shape;
	}

}