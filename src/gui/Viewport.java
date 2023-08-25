package gui;

import static constants.PhysicalConstants.cm;
import static simulation.Simulation.interactionProcessor;
import static simulation.Simulation.timeStepController;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.Timer;

import constants.PhysicalConstants;
import elements.Element;
import elements.force_pair.Spring;
import elements.groups.ParticleGroup;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;
import evaluation.Vector;
import gui.lang.GUIStrings;
import gui.shapes.SpringShape;
import simulation.Simulation;
import simulation.components.Boundaries;

public class Viewport extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -8872059106110231269L;
	public static final Color BACKGROUND = new Color(255, 255, 255);
	public static final Color GRID = new Color(200, 200, 200);
	public static final Color BOUNDARIES = new Color(200, 50, 50);
	public static final Color CROSS = Color.MAGENTA;
	public static final Color SELECTED = Color.YELLOW;
	public static final Color ARROW_VELOCITY = Color.BLUE;
	public static final Color ARROW_FORCE = Color.ORANGE;
	private static final int ARROW_DRAWING_MIN_THRESHOLD = 8;
	public static final Color FONT_MAIN = Color.BLACK;
	public static final Color FONT_TAGS = Color.BLACK;
	public static final float LABELS_MIN_FONT_SIZE = 10;
	public static final int LABELS_FONT_SIZE = 14;
	public static final float LABELS_MAX_FONT_SIZE = 32;
	private static float currentFontSize = LABELS_FONT_SIZE;
	public static final int REFRESH_MESSAGES_INTERVAL = 400;
	static final int FRAME_PAINT_DELAY = 18;
	static final int AUTOSCALE_MARGIN = 75;
	public static final double DEFAULT_GRID_SIZE = 20 * cm;
	private static boolean drawMessages = true;
	public static boolean useGrid = true;
	private static boolean drawTracks = false;
	private static boolean drawHeatMap = false;
	private ParticleGroup particles;
	private SpringGroup springs;
	private static Graphics2D globalCanvas;
	public static Graphics2D tracksCanvas;
	private static RenderingHints rh;
	public static Font labelsFont;
	private static Font mainFont;
	private static final Camera camera = new Camera();
	private static int viewportHeight, viewportWidth, fps = 0;
	private static double scale = 100;
	private static double targetScale = 10;
	private static double gridSize = DEFAULT_GRID_SIZE;
	private static double crossX = 0;
	private static double crossY = 0;
	private static long frameTime, dt;
	private static String timeString = "N/A", timeStepString = "N/A";
	private static Timer refreshLabelsTimer;
	private static BufferedImage tracksImage;
	private static BasicStroke arrowStroke = new BasicStroke(2f);
	public static BasicStroke crossStroke = new BasicStroke(3f);

	public Viewport(int initW, int initH) {
		particles = Simulation.getParticles();
		springs = Simulation.getSprings();
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		setBounds(0, 0, initW, initH);
		setDoubleBuffered(true);
		refreshStaticSizeConstants();
		initTracksImage();
		mainFont = new Font("Tahoma", Font.TRUETYPE_FONT, 14);
		labelsFont = new Font("Arial", Font.TRUETYPE_FONT, LABELS_FONT_SIZE);
		refreshLabelsTimer = new Timer(REFRESH_MESSAGES_INTERVAL, this);
	}

	@Override
	public void run() {
		refreshLabelsTimer.start();
		long sleep;
		ConsoleWindow.println(GUIStrings.RENDERING_THREAD_STARTED);
		while (true) {
			dt = System.currentTimeMillis() - frameTime;
			repaint();
			sleep = FRAME_PAINT_DELAY - dt;
			if (sleep < 0)
				sleep = 2;
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.out.println(GUIStrings.INTERRUPTED_THREAD + ": " + e.getMessage());
			}
			frameTime = System.currentTimeMillis();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		scale += (targetScale - scale) / 2;
		globalCanvas = (Graphics2D) g;
		drawWholeFrameOn(globalCanvas);
	}

	private void drawWholeFrameOn(Graphics2D graphics) {
		graphics.setRenderingHints(rh);
		camera.follow();
		currentFontSize = scaleLabelsFont();
		drawBackgroundOn(graphics);
		if (drawTracks)
			graphics.drawImage(tracksImage, 0, 0, null);
		drawBoundariesOn(graphics);
		drawSpringsOn(graphics);
		drawCirclesOn(graphics);
		graphics.setColor(FONT_TAGS);
		graphics.setStroke(arrowStroke);
		drawCrossOn(graphics, crossX, crossY, true);
		drawAxisOn(graphics);
		drawScaleLineOn(graphics);
		if (drawMessages)
			drawMessagesOn(graphics);
		Toolkit.getDefaultToolkit().sync();
		graphics.dispose();
		fps++;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == refreshLabelsTimer) {
			timeString = String.format("t = %.3f c, ", Simulation.getTime())
					+ String.format("dt = %.4f", timeStepController.getTimeStepSize() * 1000) + " ms, "
					+ String.format("Vmax = %.2f m/s", interactionProcessor.defineMaxParticleVelocity()) + ", fps = "
					+ fps * 1000 / REFRESH_MESSAGES_INTERVAL;
			double r = Simulation.interactionProcessor.getTimeStepReserveRatio();
			Simulation.timeStepController.measureTimeScale();
			double timeScale = Simulation.timeStepController.getMeasuredTimeScale();
			String displayedTimeScale = "нявызначаны";
			if (Simulation.getInstance().isActive())
				if (timeScale > 1000 || timeScale < 1e-3)
					displayedTimeScale = String.format("%.2e", timeScale);
				else
					displayedTimeScale = String.format("%.3f", timeScale);
			if (r > 100)
				timeStepString = String.format(
						GUIStrings.TIMESTEP_RESERVE + " > 100 " + GUIStrings.TIME_SCALE + " " + displayedTimeScale, r);
			else
				timeStepString = String.format(
						GUIStrings.TIMESTEP_RESERVE + " = %.1f " + GUIStrings.TIME_SCALE + " " + displayedTimeScale, r);
			MainWindow.getInstance().refreshGUIDisplays();
			Simulation.timeStepController.clearStepsPerSecond();
			fps = 0;
		}
	}

	static void drawBackgroundOn(Graphics2D targetG2d) {
		if (useGrid) {
			double gridStep = scale * gridSize;
			int gridMinorStep = (int) gridStep;
			if (gridStep >= 15 && gridStep < viewportWidth * 2) {
				BufferedImage bi = new BufferedImage(gridMinorStep, gridMinorStep, BufferedImage.TYPE_INT_RGB);
				Graphics2D big2d = bi.createGraphics();
				big2d.setColor(BACKGROUND);
				big2d.fillRect(0, 0, gridMinorStep, gridMinorStep);
				big2d.setColor(GRID);
				big2d.drawLine(0, 0, gridMinorStep, 0);
				big2d.drawLine(0, 0, 0, gridMinorStep);
				TexturePaint tp = new TexturePaint(bi,
						new Rectangle2D.Double(toScreenX(0), toScreenY(0), gridStep, gridStep));
				targetG2d.setPaint(tp);
				targetG2d.setRenderingHints(rh);
			} else {
				targetG2d.setColor(BACKGROUND);
			}
		} else
			targetG2d.setColor(BACKGROUND);
		targetG2d.fillRect(0, 0, viewportWidth, viewportHeight);
	}

	void refreshStaticSizeConstants() {
		viewportWidth = getWidth();
		viewportHeight = getHeight();
	}

	void initTracksImage() {
		tracksImage = new BufferedImage(viewportWidth, viewportHeight, BufferedImage.TYPE_INT_RGB);
		tracksCanvas = tracksImage.createGraphics();
		tracksCanvas.setStroke(new BasicStroke(2f));
		tracksCanvas.setRenderingHints(rh);
		clearTracksImage();

	}

	private void drawCirclesOn(Graphics2D targetG2d) {
		Particle p;
		for (int i = 0; i < particles.size(); i++) {
			p = particles.get(i);
			if (p.isVisible()) {
				p.getShape().paintShape(targetG2d);
			}
		}
		if (Simulation.getReferenceParticle().isVisible())
			Simulation.getReferenceParticle().getShape().paintShape(targetG2d);
		if (camera.getFollowong() != null) {
			Element following = camera.getFollowong();
			drawCrossOn(targetG2d, following.getCenterPoint().x, following.getCenterPoint().y, true);
		}
	}

	private void drawSpringsOn(Graphics2D targetG2d) {
		Spring s;
		for (int i = 0; i < springs.size(); i++) {
			s = springs.get(i);
			if (s.isVisible())
				s.getShape().paintShape(targetG2d);
		}
		targetG2d.setStroke(new BasicStroke(1f));
	}

	private void drawMessagesOn(Graphics2D targetG2d) {
		targetG2d.setColor(Color.BLACK);
		targetG2d.setFont(mainFont);
		targetG2d.drawString(timeString, 2, 12);
		targetG2d.drawString(timeStepString, 2, 28);
	}

	public static void drawStringTilted(String string, int x1, int y1, int x2, int y2) {
		double alpha = Math.atan2(x2 - x1, y1 - y2) + Math.PI / 2;
		globalCanvas.setFont(labelsFont.deriveFont(getCurrentFontSize()));
		globalCanvas.setColor(FONT_TAGS);
		int xc = Math.min(x1, x2) + (Math.max(x1, x2) - Math.min(x1, x2)) / 2;
		int yc = Math.min(y1, y2) + (Math.max(y1, y2) - Math.min(y1, y2)) / 2;
		alpha = evaluation.MyMath.fitAbsAngleRad(alpha);
		globalCanvas.translate(xc, yc);
		globalCanvas.rotate(alpha);
		globalCanvas.drawString(string, -(currentFontSize * string.length()) / 4, -currentFontSize / 2);
		globalCanvas.rotate(-alpha);
		globalCanvas.translate(-xc, -yc);
	}

	private static float scaleLabelsFont() {
		float size = (float) (scale * SpringShape.fontSize * PhysicalConstants.cm);
		if (size > LABELS_MAX_FONT_SIZE)
			size = LABELS_MAX_FONT_SIZE;
		else if (size < LABELS_MIN_FONT_SIZE)
			size = LABELS_MIN_FONT_SIZE;
		return size;
	}
	
	public static float getCurrentFontSize() {
		return currentFontSize;
	}

	private void drawBoundariesOn(Graphics2D targetG2d) {
		targetG2d.setColor(BOUNDARIES);
		targetG2d.setStroke(arrowStroke);
		Boundaries b = Simulation.getContent().getBoundaries();
		int x1 = toScreenX(b.getLeft());
		int y1 = toScreenY(b.getUpper());
		int w = toScreen(b.getWidth());
		int h = toScreen(b.getHeight());
		if (b.isUseBottom())
			targetG2d.drawLine(0, y1 + h, viewportWidth, y1 + h);
		if (b.isUseRight())
			targetG2d.drawLine(x1 + w, 0, x1 + w, viewportHeight);
		if (b.isUseLeft())
			targetG2d.drawLine(x1, 0, x1, viewportHeight);
		if (b.isUseUpper())
			targetG2d.drawLine(0, y1, viewportWidth, y1);
	}

	private void drawCrossOn(Graphics2D targetG2d, double x, double y, boolean drawTag) {
		int xc = toScreenX(x);
		int yc = toScreenY(y);
		targetG2d.setColor(CROSS);
		targetG2d.drawLine(xc, yc + 8, xc, yc - 8);
		targetG2d.drawLine(xc - 8, yc, xc + 8, yc);
		targetG2d.setFont(labelsFont);
		targetG2d.setColor(FONT_TAGS);
		if (drawTag)
			targetG2d.drawString(String.format("(%.1e", x) + String.format("; %.1e) m", y), xc + 4, yc - 4);
	}

	private void drawAxisOn(Graphics2D targetG2d) {
		int x0 = 20;
		int y0 = viewportHeight - 20;
		int x1 = x0 + 50;
		int y1 = y0 - 50;
		drawArrowLine(x0, y0, x1, y0, 10, 4);
		drawArrowLine(x0, y0, x0, y1, 10, 4);
		targetG2d.drawString("X", x1, y0 - 4);
		targetG2d.drawString("Y", x0 + 2, y1);
	}

	private void drawScaleLineOn(Graphics2D targetG2d) {
		int l = 50;
		int xc = viewportWidth - l / 2 - 20;
		int yc = viewportHeight - 20;
		targetG2d.drawLine(xc - l / 2, yc, xc + l / 2, yc);
		targetG2d.drawString(String.format("%.1e m", fromScreen(l)), xc - 32, yc - 4);
	}

	public static void drawArrowLine(int x1, int y1, Vector v, Color arrowColor, String label) {
		double length = Math.log10(v.norm() + 1) / 2;
		int dx = toScreen(length * v.defineCos());
		int dy = toScreen(length * v.defineSin());
		if (length > fromScreen(ARROW_DRAWING_MIN_THRESHOLD)) {
			globalCanvas.setColor(arrowColor);
			globalCanvas.setStroke(arrowStroke);
			drawArrowLine(x1, y1, x1 + dx, y1 - dy, 11, 4);
			if (!label.isEmpty()) {
				drawStringTilted(String.format("%.2f " + label, v.norm()), x1, y1, x1 + dx, y1 - dy);
			}
		}
	}

	private static void drawArrowLine(int x1, int y1, int x2, int y2, int d, int h) {
		int dx = x2 - x1, dy = y2 - y1;
		double D = Math.sqrt(dx * dx + dy * dy);
		double xm = D - d, xn = xm, ym = h, yn = -h, x;
		double sin = dy / D, cos = dx / D;

		x = xm * cos - ym * sin + x1;
		ym = xm * sin + ym * cos + y1;
		xm = x;

		x = xn * cos - yn * sin + x1;
		yn = xn * sin + yn * cos + y1;
		xn = x;

		int[] xpoints = { x2, (int) xm, (int) xn };
		int[] ypoints = { y2, (int) ym, (int) yn };

		globalCanvas.drawLine(x1, y1, x2, y2);
		globalCanvas.fillPolygon(xpoints, ypoints, 3);
	}

	public static void clearTracksImage() {
		if (drawTracks) {
			drawBackgroundOn((Graphics2D) tracksImage.getGraphics());
		}
	}

	public static int toScreen(double x) {
		return (int) Math.round(scale * x);
	}

	public static double fromScreen(double x) {
		return x / scale;
	}

	public static int toScreenX(double x) {
		return (int) Math.round(viewportWidth / 2 + scale * (x - camera.getX()));
	}

	public static int toScreenY(double y) {
		return (int) Math.round((viewportHeight) / 2 - scale * (y - camera.getY()));
	}

	public static double fromScreenX(int x) {
		return (-viewportWidth / 2 + x) / scale + camera.getX();
	}

	public static double fromScreenY(int y) {
		return ((viewportHeight) / 2 - y) / scale + camera.getY();
	}

	public static double getScale() {
		return scale;
	}

	public static void setScale(double newTargetScale) {
		targetScale = newTargetScale;
		clearTracksImage();
	}

	public static void setCrossX(double crossX) {
		Viewport.crossX = crossX;
	}

	public static void setCrossY(double crossY) {
		Viewport.crossY = crossY;
	}

	public static double getGridSize() {
		return gridSize;
	}

	public static void setGridSize(double size) {
		if (size < 0)
			size = 20 * cm;
		gridSize = size;
		MainWindow.getInstance().refreshGUIControls();
		ConsoleWindow.println(String.format(GUIStrings.GRID_SIZE + "%.2e m", gridSize));
	}

	public static boolean isDrawTracks() {
		return drawTracks;
	}

	public static void setDrawTracks(boolean b) {
		drawTracks = b;
		clearTracksImage();
		ConsoleWindow.println(GUIStrings.DRAW_TRACKS + ": " + b);
	}

	public static boolean isDrawFields() {
		return drawHeatMap;
	}

	public static void scaleToAllParticles() {
		if (Simulation.getParticlesCount() > 0) {
			Boundaries b = Simulation.getContent().getBoundaries();
			b.refreshEffectiveBoundaries();
			double h = b.getEffectiveHeight();
			double w = b.getEffectiveWidth();
			setScale(Math.min((viewportHeight - AUTOSCALE_MARGIN) / h, (viewportWidth - AUTOSCALE_MARGIN) / w));
			System.out.println("viewportH " + viewportHeight);
			System.out.println("viewportW " + viewportWidth);
			camera.following = null;
			camera.setX(b.getEffectiveCenterX());
			camera.setY(b.getEffectiveCenterY());
		} else
			scaleToBoundaries();
	}

	public static void scaleToBoundaries() {
		Boundaries b = Simulation.getContent().getBoundaries();
		if (!b.isUseLeft() || !b.isUseRight() || !b.isUseBottom())
			scaleToAllParticles();
		else {
			double h = b.getHeight();
			double w = b.getWidth();
			setScale(Math.min((viewportHeight - 4) / h, (viewportWidth - 2) / w));
			camera.following = null;
			camera.setX(b.getLeft() + b.getWidth() / 2);
			camera.setY(b.getBottom() + b.getHeight() / 2);
			clearTracksImage();
		}
	}

	public static MouseMode getMouseMode() {
		return ViewportEvent.mouseMode;
	}

	public static void setMouseMode(MouseMode mouseMode) {
		ViewportEvent.mouseMode = mouseMode;
		Simulation.clearSelection();
		ConsoleWindow.println(GUIStrings.MOUSE_MODE + ": " + ViewportEvent.mouseMode);
	}

	public void saveImageToFile() {
		BufferedImage buffer = new BufferedImage(viewportWidth, viewportHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D ig2 = buffer.createGraphics();
		drawWholeFrameOn(ig2);
		String fileName = String.format(GUIStrings.SCREENSHOT_NAME + "_%.3fс.jpg", Simulation.getTime());
		try {
			if (javax.imageio.ImageIO.write(buffer, "JPEG", new java.io.File(fileName)))
				ConsoleWindow.println(GUIStrings.IMAGE_SAVED_TO + " " + fileName);
		} catch (IOException e) {
			MainWindow.imageWriteErrorMessage(fileName);
		}
	}

	public static Graphics2D getCanvas() {
		return globalCanvas;
	}

	public static Camera getCamera() {
		return camera;
	}

	public static void reset() {
		refreshLabelsTimer.restart();
		scale = 1e-6;
	}
}