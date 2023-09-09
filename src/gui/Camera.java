package gui;

import elements.Element;

public class Camera {

	public static final int CAMERA_WATCH_SMOOTH = 12;
	public static final int CAMERA_KEYBOARD_SPEED = 15;
	public static final float CAMERA_ZOOM_INCREMENT = 1.25f;
	double x, y; 
	double vx = 0, vy = 0;
	private Element following;
	private Viewport viewport;
	
	public Camera(Viewport v) {
		this.viewport = v;
	}

	public double getX() {
		return x;
	}

	public void setX(double cameraX) {
		x = cameraX;
		viewport.clearTracksImage();
	}

	public double getY() {
		return y;
	}

	public void setY(double cameraY) {
		y = cameraY;
		viewport.clearTracksImage();
	}

	public Element getFollowing() {
		return following;
	}

	public void setFollowing(Element element) {
		this.following = element;
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
		viewport.clearTracksImage();
	}

	public double getVy() {
		return vy;
	}

	public void setVy(double vy) {
		this.vy = vy;
		viewport.clearTracksImage();
	}

	public void follow() {
		if (following != null) {
			double xp = following.getCenterPoint().x;
			double yp = following.getCenterPoint().y;
			x -= (x - xp) / CAMERA_WATCH_SMOOTH;
			y -= (y - yp) / CAMERA_WATCH_SMOOTH;
		} else {
			x += vx;
			y += vy;
		}
	}

}
