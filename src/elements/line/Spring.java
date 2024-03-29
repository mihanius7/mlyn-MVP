package elements.line;

import static calculation.Functions.cube;
import static calculation.Functions.defineDistance;
import static calculation.Functions.defineReducedMass;
import static calculation.Functions.sqr;
import static calculation.constants.PhysicalConstants.cm;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;

import calculation.Functions;
import calculation.constants.PhysicalConstants;
import elements.Element;
import elements.point.Particle;
import gui.ConsoleWindow;
import gui.lang.GUIStrings;
import gui.shapes.SpringShape;
import simulation.Simulation;

public class Spring extends Pair implements Element {

	public static double DEFAULT_VISIBLE_WIDTH = 2 * cm;
	protected double l0 = 0, k = 0, c = 0, u2 = 0, dx = 0;
	protected double visibleWidth = DEFAULT_VISIBLE_WIDTH;
	protected double fn;
	protected double breakUpTension = Double.MAX_VALUE;
	protected boolean visible = true, isSelected = false, isLine = true, canCollide = false;
	protected GapType gapType = GapType.NONE;
	
	public static double maxSpringForce;
	public static double maxSpringForceCandidate;
	
	protected SpringShape shape;

	public Spring(Particle i, Particle j, double l0, double k, double c, double u2) {
		super(i, j);
		initSpring(k, l0, c);
		this.u2 = u2;
		distance = defineDistance(i, j);
	}

	public Spring(int i, int j, double l0, double k, double c, double u2) {
		super(i, j);
		initSpring(k, l0, c);
		this.u2 = u2;
		distance = defineDistance(i, j);
	}

	public Spring(Particle i, Particle j, double k, double c) {
		this(i, j, defineDistance(i, j), k, c, 0);
	}

	public Spring(int i, int j, double k, double c) {
		this(i, j, defineDistance(i, j), k, c, 0);
	}

	private void initSpring(double k, double l0, double c) {
		this.k = k;
		this.l0 = l0;
		this.c = c;
		distance = l0;
		lastDistance = distance;
		shape = new SpringShape(this);
		ConsoleWindow.println(GUIStrings.SPRING_CREATED + ": ");
		refreshResonantFrequency();
		ConsoleWindow.println(String.format("	" + GUIStrings.SPRING_DAMPING_RATIO + " %.3f", defineDampingRatio()));
		checkOverCriticalDamping();
	}

	public double getAbsoluteDeformation() {
		return dx;
	}

	private void defineSpringForce() {
		dx = distance - l0;
		double fd = defineVelocityProjection() * c;
		double fs;
		if (u2 == 0)
			fs = -k * dx;
		else
			fs = -k * (dx + u2 * cube(dx));
		force = fs + fd;
	}

	@Override
	public void doForce() {
		super.doForce();
		defineSpringForce();
		Functions.addForce(p1, p2, force, distance);
		if (Math.abs(force) > maxSpringForceCandidate)
			maxSpringForceCandidate = Math.abs(force);
		if (force >= breakUpTension)
			Simulation.getInstance().removeSafety(this);
	}

	public double getVisibleWidth() {
		return visibleWidth;
	}

	public double getParticlesMeanWidth() {
		return (p1.getRadius() + p2.getRadius()) / 2;
	}

	public double getNominalLength() {
		return l0;
	}

	public void setNominalLength(double length) {
		this.l0 = length;
		ConsoleWindow.println(String.format(GUIStrings.SPRING_NOMINAL_LENGTH + " %.6e", length));
	}

	public double getDeformatedLength() {
		return l0 + dx;
	}

	public void saveDeformation(double coeficient) {
		this.l0 += coeficient * dx;
	}

	public double getStiffnes() {
		return k;
	}

	public void setStiffnes(double k) {
		this.k = k;
		refreshResonantFrequency();
		ConsoleWindow.println(String.format(GUIStrings.SPRING_STIFFNES + "  %.3f", k));
	}

	public double getHardening() {
		return sqrt(u2);
	}

	public void setHardening(double u) {
		this.u2 = sqr(u);
		ConsoleWindow.println(GUIStrings.SPRING_HARDENING_COEFFICIENT + " " + u);
	}

	public double getBreakUpTension() {
		return breakUpTension;
	}

	public void setBreakUpTension(double maxTension) {
		this.breakUpTension = maxTension;
	}

	public double refreshResonantFrequency() {
		fn = (1 / (2 * PI)) * sqrt(k / defineReducedMass(p1, p2));
		return fn;
	}

	public double refreshGravityResonantFrequency() {
		fn = (1 / (2 * PI)) * sqrt(Math.abs(PhysicalConstants.gn / dx));
		return fn;
	}

	public double getResonantFrequency() {
		return fn;
	}

	public void setResonantFrequency(double f) {
		if (f > 0)
			k = 4 * sqr(Math.PI) * sqr(f) * defineReducedMass(p1, p2);
		ConsoleWindow.println(String.format(GUIStrings.SPRING_STIFFNES + " %.3e N/m", k));
		refreshResonantFrequency();
		ConsoleWindow.println(String.format(GUIStrings.SPRING_RESONANT_FREQUENCY + " %.1f Hz", fn));
	}

	public double defineDampingRatio() {
		return getDamping() / defineCriticalDamping();
	}

	public double defineCriticalDamping() {
		return 2 * sqrt(k * defineReducedMass(p1, p2));
	}

	public double getDamping() {
		return c;
	}

	public void setDamping(double c) {
		this.c = c;
	}

	public void setDampingCritical() {
		setDampingRatio(1.0);
	}

	public void setDampingRatio(double ksi) {
		setDamping(ksi * defineCriticalDamping());
		checkOverCriticalDamping();
		ConsoleWindow.println(String.format(GUIStrings.SPRING_DAMPING_RATIO + " %.3f", defineDampingRatio())
				+ String.format(", " + GUIStrings.SPRING_DAMPING + " %.3e N/(m/s)", c));
	}

	public void setQualityFactor(double Q) {
		if (Q > 0) {
			ConsoleWindow.println(String.format(GUIStrings.SPRING_QUALITY_FACTOR + " %.1f...", Q));
			setDampingRatio(1f / 2 / Q);
		}
	}

	public void setVisibleWidth(double visibleWidth) {
		this.visibleWidth = visibleWidth;
	}

	private void checkOverCriticalDamping() {
		if (defineDampingRatio() > 20) {
			setDampingRatio(20);
			ConsoleWindow.println(GUIStrings.HYPERCRITICAL_DAMPING_FIXED);
		}
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

	public void setIsLine(boolean b) {
		isLine = b;
	}

	public boolean isLine() {
		return isLine;
	}

	@Override
	public boolean isCanCollide() {
		return canCollide;
	}

	@Override
	public void setCanCollide(boolean b) {
		canCollide = b;
		if (canCollide)
			visibleWidth = 5 * cm;
		else
			visibleWidth = DEFAULT_VISIBLE_WIDTH;
	}

	public SpringShape getShape() {
		return shape;
	}

	@Override
	public java.awt.geom.Point2D.Double getCenterPoint() {
		return new Point2D.Double(0.5 * p1.getX() + 0.5 * p2.getX(), 0.5 * p1.getY() + 0.5 * p2.getY());
	}
}
