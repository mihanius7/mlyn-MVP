package simulation;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.m;
import static java.lang.Math.abs;

import elements.point.Particle;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;

public class Boundaries implements Cloneable {
	private static final double INITIAL_BOTTOM_BORDER = 0 * m;
	private static final double INITIAL_UPPER_BORDER = 4.8 * m;
	private static final double INITIAL_RIGHT_BORDER = 8.5 * m;
	private static final double INITIAL_LEFT_BORDER = 0 * m;
	private double left, right, upper, bottom;
	private double leftEffective, rightEffective, upperEffective, bottomEffective;
	private double autosizeMargin = 10 * cm;
	private boolean useLeft, useRight, useUpper, useBottom;

	public Boundaries() {
		this.left = INITIAL_LEFT_BORDER;
		this.right = INITIAL_RIGHT_BORDER;
		this.upper = INITIAL_UPPER_BORDER;
		this.bottom = INITIAL_BOTTOM_BORDER;
		this.useLeft = true;
		this.useRight = true;
		this.useUpper = false;
		this.useBottom = true;
	}

	public void setBounds(double left, double right, double upper, double bottom) {
		this.left = left;
		ConsoleWindow.println(GUIStrings.LEFT_BOUNDARY + ", m: " + this.left);
		this.right = right;
		ConsoleWindow.println(GUIStrings.RIGHT_BOUNDARY + ", m: " + this.right);
		this.upper = upper;
		ConsoleWindow.println(GUIStrings.UPPER_BOUNDARY + ", m: " + this.upper);
		this.bottom = bottom;
		ConsoleWindow.println(GUIStrings.BOTTOM_BOUNDARY + ", m: " + this.bottom);
	}

	public double getLeft() {
		return left;
	}

	public double getRight() {
		return right;
	}

	public double getUpper() {
		return upper;
	}

	public double getBottom() {
		return bottom;
	}

	public double getHeight() {
		return upper - bottom;
	}

	public double getWidth() {
		return right - left;
	}

	public void setHeight(double h) {
		upper = bottom + h;
	}

	public void setWidth(double w) {
		right = left + w;
	}

	public boolean isUseLeft() {
		return useLeft;
	}

	public void setUseLeft(boolean useLeft) {
		this.useLeft = useLeft;
	}

	public void setUseAll(boolean useAll) {
		this.useLeft = useAll;
		this.useRight = useAll;
		this.useUpper = useAll;
		this.useBottom = useAll;
	}

	public boolean isUseRight() {
		return useRight;
	}

	public void setUseRight(boolean useRight) {
		this.useRight = useRight;
	}

	public boolean isUseUpper() {
		return useUpper;
	}

	public void setUseUpper(boolean useUpper) {
		this.useUpper = useUpper;
	}

	public boolean isUseBottom() {
		return useBottom;
	}

	public void setUseBottom(boolean useBottom) {
		this.useBottom = useBottom;
	}

	public boolean isInside(double x, double y) {
		if (x >= left && x <= right && y >= upper && y <= bottom)
			return true;
		else
			return false;
	}

	public void refreshEffectiveBoundaries() {
		leftEffective = Double.MAX_VALUE;
		bottomEffective = Double.MAX_VALUE;
		rightEffective = Double.MIN_VALUE;
		upperEffective = Double.MIN_VALUE;
		for (int i = 0; i < Simulation.getInstance().getContent().getParticlesCount(); i++) {
			if (Simulation.getInstance().getContent().getParticle(i).getX() - Simulation.getInstance().getContent().getParticle(i).getRadius() < leftEffective)
				leftEffective = Simulation.getInstance().getContent().getParticle(i).getX() - Simulation.getInstance().getContent().getParticle(i).getRadius();
			if (Simulation.getInstance().getContent().getParticle(i).getX() + Simulation.getInstance().getContent().getParticle(i).getRadius() > rightEffective)
				rightEffective = Simulation.getInstance().getContent().getParticle(i).getX() + Simulation.getInstance().getContent().getParticle(i).getRadius();
			if (Simulation.getInstance().getContent().getParticle(i).getY() - Simulation.getInstance().getContent().getParticle(i).getRadius() < bottomEffective)
				bottomEffective = Simulation.getInstance().getContent().getParticle(i).getY() - Simulation.getInstance().getContent().getParticle(i).getRadius();
			if (Simulation.getInstance().getContent().getParticle(i).getY() + Simulation.getInstance().getContent().getParticle(i).getRadius() > upperEffective)
				upperEffective = Simulation.getInstance().getContent().getParticle(i).getY() + Simulation.getInstance().getContent().getParticle(i).getRadius();
		}
	}

	public Object clone() throws CloneNotSupportedException {
		Boundaries clone = (Boundaries) super.clone();
		return clone;
	}

	public void autosize() {
		refreshEffectiveBoundaries();
		setBounds(leftEffective - autosizeMargin, rightEffective + autosizeMargin, upperEffective + autosizeMargin,
				bottomEffective - autosizeMargin);
	}

	public double getEffectiveWidth() {
		return abs(rightEffective - leftEffective);
	}

	public double getEffectiveHeight() {
		return abs(upperEffective - bottomEffective);
	}

	public double getEffectiveCenterX() {
		return leftEffective + 0.5 * getEffectiveWidth();
	}

	public double getEffectiveCenterY() {
		return bottomEffective + 0.5 * getEffectiveHeight();
	}


	public void applyBoundaryConditions(Particle p) {

		if (isUseRight() && p.getX() + p.getRadius() > getRight()) {
			p.getVelocityVector().multiplyX(-p.getElasticity());
			p.setX(getRight() - p.getRadius());
			p.getVelocityVector().multiplyY(0.95);
		} else if (isUseLeft() && p.getX() - p.getRadius() < getLeft()) {
			p.getVelocityVector().multiplyX(-p.getElasticity());
			p.setX(getLeft() + p.getRadius());
			p.getVelocityVector().multiplyY(0.95);
		}

		if (isUseBottom() && getBottom() > p.getY() - p.getRadius()) {
			double newvy = -p.getVelocityVector().Y() * p.getElasticity();
			if (p.getVelocityVector().Y() < -1E-6)
				p.setVy(newvy);
			else
				p.setVy(0);
			p.setY(getBottom() + p.getRadius());
			p.getVelocityVector().multiplyX(0.95);
		} else if (isUseUpper() && p.getY() + p.getRadius() > getUpper()) {
			p.getVelocityVector().multiplyY(-p.getElasticity());
			p.setY(getUpper() - p.getRadius());
			p.getVelocityVector().multiplyX(0.95);
		}
	}
}
