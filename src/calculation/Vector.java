package calculation;

import static java.lang.Math.sqrt;

public class Vector implements Cloneable {
	private double x, y;

	public Vector(double xComponent, double yComponent) {
		x = xComponent;
		y = yComponent;
	}

	public Vector(Vector v) {
		x = v.X();
		y = v.Y();
	}

	public Vector() {
		this(0, 0);
	}

	public void setX(double xComponent) {
		x = xComponent;
	}

	public void setY(double yComponent) {
		y = yComponent;
	}

	public void setXY(double xComponent, double yComponent) {
		x = xComponent;
		y = yComponent;
	}

	public double X() {
		return x;
	}

	public double Y() {
		return y;
	}

	public void addToX(double dx) {
		x += dx;
	}

	public void addToY(double dy) {
		y += dy;
	}

	public void addToXY(double dx, double dy) {
		x += dx;
		y += dy;
	}

	public Vector multiply(double d) {
		multiplyX(d);
		multiplyY(d);
		return this;
	}

	public void multiplyX(double d) {
		x *= d;
	}

	public void multiplyY(double d) {
		y *= d;
	}

	public double defineAngle() {
		return Functions.angle(x, y);
	}

	public Vector add(Vector v) {
		x += v.X();
		y += v.Y();
		return this;
	}

	public Vector subtract(Vector v) {
		x -= v.X();
		y -= v.Y();
		return this;
	}

	public double norm() {
		return sqrt(x * x + y * y);
	}

	public double normSquared() {
		return (x * x + y * y);
	}

	public double defineCos() {
		return x / norm();
	}

	public double defineSin() {
		return y / norm();
	}

	public Vector clone() throws CloneNotSupportedException {
		return (Vector) super.clone();
	}

}
