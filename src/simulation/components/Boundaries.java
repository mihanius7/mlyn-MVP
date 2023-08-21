package simulation.components;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.m;
import static java.lang.Math.abs;
import static simulation.Simulation.getParticle;
import static simulation.Simulation.getParticlesCount;

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
		for (int i = 0; i < getParticlesCount(); i++) {
			if (getParticle(i).getX() - getParticle(i).getRadius() < leftEffective)
				leftEffective = getParticle(i).getX() - getParticle(i).getRadius();
			if (getParticle(i).getX() + getParticle(i).getRadius() > rightEffective)
				rightEffective = getParticle(i).getX() + getParticle(i).getRadius();
			if (getParticle(i).getY() - getParticle(i).getRadius() < bottomEffective)
				bottomEffective = getParticle(i).getY() - getParticle(i).getRadius();
			if (getParticle(i).getY() + getParticle(i).getRadius() > upperEffective)
				upperEffective = getParticle(i).getY() + getParticle(i).getRadius();
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
}
