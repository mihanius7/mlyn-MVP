package gui;

import elements.point_mass.Particle;

public class Camera {

	public static final int CAMERA_WATCH_SMOOTH = 8;
	public static final int CAMERA_KEYBOARD_SPEED = 15;
	public static final float CAMERA_ZOOM_INCREMENT = 1.25f;

	double x, y, vx = 0, vy = 0;
	Particle watchParticle;

	public double getX() {
		return x;
	}

	public void setX(double cameraX) {
		x = cameraX;
	}

	public double getY() {
		return y;
	}

	public void setY(double cameraY) {
		y = cameraY;
	}

	public Particle getWatchParticle() {
		return watchParticle;
	}

	public void setWatchParticle(Particle watchParticle) {
		this.watchParticle = watchParticle;
	}

	public void addX(double dx) {
		x += dx;
	}

	public void addY(double dy) {
		y += dy;
	}

	public void addXWithRollingMean(double dx) {
		x -= (x - dx) / 3;
	}

	public void addYWithRollingMean(double dy) {
		y -= (y - dy) / 3;
	}

	public double getVx() {
		return vx;
	}

	public void setVx(double vx) {
		this.vx = vx;
		Viewport.clearTracksImage();
	}

	public double getVy() {
		return vy;
	}

	public void setVy(double vy) {
		this.vy = vy;
		Viewport.clearTracksImage();
	}

	public void watch() {
		if (watchParticle != null) {
			double xp = watchParticle.getX() + 0.05 * watchParticle.getVx();
			double yp = watchParticle.getY() + 0.05 * watchParticle.getVy();
			x -= (x - xp) / CAMERA_WATCH_SMOOTH;
			y -= (y - yp) / CAMERA_WATCH_SMOOTH;
		} else {
			x += vx;
			y += vy;
		}
	}

}
