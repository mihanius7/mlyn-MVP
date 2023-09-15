package elements.point;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import elements.Element;
import gui.shapes.ParticleShape;
import simulation.math.Functions;

public class Particle extends PointMass implements Cloneable, Element {

	public static final double PARTICLE_ELASTICITY_DEFAULT = 0.99;
	protected double r, q;
	protected boolean visible = true, canCollide = true, isSelected = false;
	protected double frictionForce, stictionForce;
	protected double elasticity = PARTICLE_ELASTICITY_DEFAULT;

	protected ParticleShape shape;

	public Particle(double x, double y, double m, double q, double vx, double vy, double radius, Color c) {
		super(x, y, m);
		this.q = q;
		velocity.setX(vx);
		velocity.setY(vy);
		this.r = radius;
		lastx = x;
		lasty = y;
		lastVelocity.setXY(velocity.X(), velocity.Y());
		shape = new ParticleShape(this);
		shape.setColor(c);
	}

	public Particle(double x, double y, double vx, double vy, double m, double radius) {
		this(x, y, m, vx, vy, 0, radius, ParticleShape.PARTICLE_DEFAULT);
	}

	public Particle(double x, double y, double m, double radius) {
		this(x, y, m, 0, 0, 0, radius, ParticleShape.PARTICLE_DEFAULT);
	}

	public Particle(double x, double y, Particle referenceParticle) {
		this(x, y, referenceParticle.getMass(), referenceParticle.getCharge(), referenceParticle.getVx(),
				referenceParticle.getVy(), referenceParticle.getRadius(), referenceParticle.getShape().getColor());
		this.movableX = referenceParticle.isMovableX();
		this.movableY = referenceParticle.isMovableY();
		this.elasticity = referenceParticle.getElasticity();
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

	public boolean isStictionReached() {
		return lastForce.normSquared() > stictionForce * stictionForce;
	}

	public void snapToGrid(double gridSize) {
		x = Functions.roundTo(x, 1 / gridSize);
		y = Functions.roundTo(y, 1 / gridSize);
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

	public double getElasticity() {
		return elasticity;
	}

	public void setElasticity(double e) {
		this.elasticity = (e > 1) ? 1 : (e < 0) ? 0 : e;
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

	public void setCanCollide(boolean b) {
		this.canCollide = b;
	}

	public ParticleShape getShape() {
		return shape;
	}

	@Override
	public Double getCenterPoint() {
		return new Point2D.Double(x, y);
	}

}