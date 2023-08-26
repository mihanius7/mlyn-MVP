package gui;

public class CoordinateConverter {
	private static Viewport viewport;
	
	public CoordinateConverter(Viewport v) {
		CoordinateConverter.viewport = v;
	}

	public static int toScreen(double x) {
		return (int) Math.round(viewport.getScale() * x);
	}

	public static double fromScreen(double x) {
		return x / viewport.getScale();
	}

	public static int toScreenX(double x) {
		return (int) Math.round(viewport.getWidth() / 2 + viewport.getScale() * (x - viewport.camera.getX()));
	}

	public static int toScreenY(double y) {
		return (int) Math.round((viewport.getHeight()) / 2 - viewport.getScale() * (y - viewport.camera.getY()));
	}

	public static double fromScreenX(int x) {
		return (-viewport.getWidth() / 2 + x) / viewport.getScale() + viewport.camera.getX();
	}

	public static double fromScreenY(int y) {
		return ((viewport.getHeight()) / 2 - y) /viewport.getScale() + viewport.camera.getY();
	}

	public static double getScale() {
		return viewport.getScale();
	}
}
