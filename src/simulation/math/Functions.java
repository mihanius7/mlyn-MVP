package simulation.math;

import static java.lang.Math.*;

import elements.line.Spring;
import elements.point.Particle;
import simulation.Simulation;

public class Functions {

	private static Vector result;

	public Functions() {
		result = new Vector();
	}

	public static double sqr(double x) {
		return x * x;
	}

	public static double cube(double x) {
		return x * x * x;
	}

	public static double angle(double dx, double dy) {
		return Math.atan2(dx, dy);
	}

	public static double defineSquaredDistance(int i, int j) {
		return (pow(Simulation.getInstance().x(i) - Simulation.getInstance().x(j), 2) + pow(Simulation.getInstance().y(i) - Simulation.getInstance().y(j), 2));
	}

	public static double defineDistance(int i, int j) {
		return fastSqrt(defineSquaredDistance(i, j));
	}

	public static double defineDistance(Particle i, Particle j) {
		return fastSqrt(defineSquaredDistance(i, j));
	}

	public static double defineSquaredDistance(Particle i, Particle j) {
		return pow(i.getX() - j.getX(), 2) + pow(i.getY() - j.getY(), 2);
	}

	public static double defineDistance(Particle p, double x, double y) {
		return sqrt(pow(p.getX() - x, 2) + pow(p.getY() - y, 2));
	}

	public static double defineSquaredDistance(Particle p, double x, double y) {
		return pow(p.getX() - x, 2) + pow(p.getY() - y, 2);
	}

	public static double defineDistanceToLine(Spring s, double x, double y) {
		Particle p1 = s.getFirstParticle();
		Particle p2 = s.getSecondParticle();
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		return Math.abs(x * (y1 - y2) + y * (x2 - x1) + (x1 * y2 - x2 * y1)) / s.getDeformatedLength();
	}

	public static double defineDistanceV2(Spring s, double x, double y) {
		Particle p1 = s.getFirstParticle();
		Particle p2 = s.getSecondParticle();
		return defineDistance(p1, x, y) + defineDistance(p2, x, y);
	}

	public static double defineDistanceToLineSegment(Spring s, double x, double y) {
		Particle p1 = s.getFirstParticle();
		Particle p2 = s.getSecondParticle();
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double dot;
		dot = dot(x - x1, y - y1, x2 - x1, y2 - y1);
		if (dot <= 0) {
			return defineDistance(p1, x, y);
		}
		dot = dot(x - x2, y - y2, x2 - x1, y2 - y1);
		if (dot >= 0) {
			return defineDistance(p2, x, y);
		} else
			return Math.abs((x * (y1 - y2) + y * (x2 - x1) + (x1 * y2 - x2 * y1)) / s.getDeformatedLength());
	}

	public static double dot(double x1, double y1, double x2, double y2) {
		return x1 * x2 + y1 * y2;
	}

	public static double dot(Vector v1, Vector v2) {
		return dot(v1.X(), v2.X(), v1.Y(), v2.Y());
	}

	public static double defineReducedMass(Particle i, Particle j) {
		return (i.getMass() * j.getMass()) / (i.getMass() + j.getMass());
	}

	public static double linear2DInterpolation(double x1, double y1, double x2, double y2, double x) {
		double returnValue;
		if (x <= x2 && x >= x1)
			returnValue = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
		else if (x > x2)
			returnValue = y2;
		else
			returnValue = y1;
		return returnValue;
	}

	public static float fastInvSqrt(float d) {
		float xhalf = 0.5f * d; // x пополам
		int i = Float.floatToIntBits(d); // битовое представление числа
		i = 0x5f3759d5 - (i >> 1); // отрицательные x не волнуют
		d = Float.intBitsToFloat(i); // вот оно, первое прибл. значение
		d = d * (1.5f - xhalf * d * d);
		return d;
	}

	public static double fastSqrt(double d) {
		return Math.sqrt(d);
	}

	public static double roundTo(double x, double a) {
		return Math.round(x * a) / a;
	}

	public static Vector summVectors(Vector v1, Vector v2) {
		result.setXY(v1.X() + v2.X(), v1.Y() + v2.Y());
		return result;
	}

	public static Vector subtractVectors(Vector v1, Vector v2) {
		result.setXY(v1.X() - v2.X(), v1.Y() - v2.Y());
		return result;
	}

	public static Vector multipleVector(Vector v1, double d) {
		result.setXY(v1.X() * d, v1.Y() * d);
		return result;
	}

	public static Vector divideVector(Vector v1, double d) {
		result.setXY(v1.X() / d, v1.Y() / d);
		return result;
	}

	public static Vector normalizeVector(Vector v) {
		Vector result = new Vector();
		double length = v.norm();
		if (length >= 0) {
			result.setX(v.X() / length);
			result.setY(v.Y() / length);
		}
		return result;
	}

	public static double fitAngleRad(double a) {
		if (a > Math.PI)
			a = a - 2 * Math.PI;
		else if (a < -Math.PI)
			a = a + 2 * Math.PI;
		return a;
	}

	public static double fitAngleDeg(double a) {
		if (a > 180)
			a = a - 360;
		else if (a < -180)
			a = a + 360;
		return a;
	}

	public static double fitAbsAngleRad(double a) {
		if (Math.abs(a) > Math.PI / 2)
			a = a + Math.PI;
		return a;
	}
	
	public static void addForce(Particle i, Particle j, double force, double distance) {
		double forceX = force * (j.getX() - i.getX()) / distance;
		double forceY = force * (j.getY() - i.getY()) / distance;
		i.addFx(-forceX);
		j.addFx(forceX);
		i.addFy(-forceY);
		j.addFy(forceY);
	}

	public static void addForceTangential(Particle i, Particle j, double force, double distance) {
		double forceX = force * (j.getY() - i.getY()) / distance;
		double forceY = force * (j.getX() - i.getX()) / distance;
		i.addFx(forceX);
		j.addFx(-forceX);
		i.addFy(-forceY);
		j.addFy(forceY);
	}

	public static void addForceAngled(Particle i, Particle j, double force, double angle) {
		double forceX = force * Math.cos(angle);
		double forceY = force * Math.sin(angle);
		i.addFx(forceX);
		j.addFx(-forceX);
		i.addFy(-forceY);
		j.addFy(forceY);
	}

	public static Vector centreOfMass(Particle[] pp) {
		double mass = 0;
		double mi, xi, yi;
		result.setXY(0, 0);
		for (int i = 0; i < pp.length; i++)
			mass += pp[i].getMass();
		for (int i = 0; i < pp.length; i++) {
			mi = pp[i].getMass();
			xi = pp[i].getX();
			yi = pp[i].getY();
			result.addToX(xi * mi / mass);
			result.addToY(yi * mi / mass);
		}
		return result;
	}

}
