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

	private final long serialVersionUID = -8872059106110231269L;
	private final int ARROW_DRAWING_MIN_THRESHOLD = 8;
	public final float LABELS_MIN_FONT_SIZE = 10;
	public final int LABELS_FONT_SIZE = 14;
	public final float LABELS_MAX_FONT_SIZE = 32;
	private float currentFontSize = LABELS_FONT_SIZE;
	public final static int REFRESH_MESSAGES_INTERVAL = 400;
	final int FRAME_PAINT_DELAY = 18;
	final int AUTOSCALE_MARGIN = 75;
	public final static double DEFAULT_GRID_SIZE = 20 * cm;
	private boolean drawMessages = true;
	public boolean useGrid = true;
	private boolean drawTracks = false;
	private boolean drawHeatMap = false;
	private ParticleGroup particles;
	private SpringGroup springs;
	Camera camera;
	CoordinateConverter coordinateConverter;
	private Graphics2D globalCanvas;
	public Graphics2D tracksCanvas;
	private RenderingHints rh;
	public Font labelsFont;
	private Font mainFont;
	private int fps = 0;
	private double scale = 100;
	private double targetScale = 10;
	private double gridSize = DEFAULT_GRID_SIZE;
	private double crossX = 0;
	private double crossY = 0;
	private long frameTime, dt;
	private String timeString = "N/A", timeStepString = "N/A";
	private Timer refreshLabelsTimer;
	private BufferedImage tracksImage;
	private BasicStroke arrowStroke = new BasicStroke(2f);
	public BasicStroke crossStroke = new BasicStroke(3f);
	private ViewportEvent viewportEvent;

	public Viewport(int initW, int initH, MainWindow mw) {
		particles = Simulation.getParticles();
		springs = Simulation.getSprings();
		viewportEvent = new ViewportEvent(this, mw);
		addKeyListener(viewportEvent);
		addMouseListener(viewportEvent);
		addMouseMotionListener(viewportEvent);
		addMouseWheelListener(viewportEvent);
		camera = new Camera(this);
		coordinateConverter = new CoordinateConverter(this);
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
		setBounds(0, 0, initW, initH);
		setDoubleBuffered(true);
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
		camera.follow();
		currentFontSize = scaleLabelsFont();
		drawBackgroundOn(graphics);
		if (drawTracks)
			graphics.drawImage(tracksImage, 0, 0, null);
		drawBoundariesOn(graphics);
		drawSpringsOn(graphics);
		drawCirclesOn(graphics);
		graphics.setColor(Colors.FONT_TAGS);
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

	void drawBackgroundOn(Graphics2D targetG2d) {
		if (useGrid) {
			double gridStep = scale * gridSize;
			int gridMinorStep = (int) gridStep;
			if (gridStep >= 15 && gridStep < getWidth() * 2) {
				BufferedImage bi = new BufferedImage(gridMinorStep, gridMinorStep, BufferedImage.TYPE_INT_RGB);
				Graphics2D big2d = bi.createGraphics();
				big2d.setColor(Colors.BACKGROUND);
				big2d.fillRect(0, 0, gridMinorStep, gridMinorStep);
				big2d.setColor(Colors.GRID);
				big2d.drawLine(0, 0, gridMinorStep, 0);
				big2d.drawLine(0, 0, 0, gridMinorStep);
				TexturePaint tp = new TexturePaint(bi,
						new Rectangle2D.Double(coordinateConverter.toScreenX(0), coordinateConverter.toScreenY(0), gridStep, gridStep));
				targetG2d.setPaint(tp);
				targetG2d.setRenderingHints(rh);
			} else {
				targetG2d.setColor(Colors.BACKGROUND);
			}
		} else
			targetG2d.setColor(Colors.BACKGROUND);
		targetG2d.fillRect(0, 0, getWidth(), getHeight());
	}

	void initTracksImage() {
		tracksImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
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
				p.getShape().paintShape(targetG2d, this);
			}
		}
		if (Simulation.getReferenceParticle().isVisible())
			Simulation.getReferenceParticle().getShape().paintShape(targetG2d, this);
		if (camera.getFollowong() != null) {
			Element following = camera.getFollowong();
			drawCrossOn(targetG2d, following.getCenterPoint().x, following.getCenterPoint().y, false);
		}
	}

	private void drawSpringsOn(Graphics2D targetG2d) {
		Spring s;
		for (int i = 0; i < springs.size(); i++) {
			s = springs.get(i);
			if (s.isVisible())
				s.getShape().paintShape(targetG2d, this);
		}
		targetG2d.setStroke(new BasicStroke(1f));
	}

	private void drawMessagesOn(Graphics2D targetG2d) {
		targetG2d.setColor(Color.BLACK);
		targetG2d.setFont(mainFont);
		targetG2d.drawString(timeString, 2, 12);
		targetG2d.drawString(timeStepString, 2, 28);
	}

	public void drawStringTilted(Graphics2D targetG2d, String string, int x1, int y1, int x2, int y2) {
		double alpha = Math.atan2(x2 - x1, y1 - y2) + Math.PI / 2;
		targetG2d.setFont(labelsFont.deriveFont(getCurrentFontSize()));
		targetG2d.setColor(Colors.FONT_TAGS);
		int xc = Math.min(x1, x2) + (Math.max(x1, x2) - Math.min(x1, x2)) / 2;
		int yc = Math.min(y1, y2) + (Math.max(y1, y2) - Math.min(y1, y2)) / 2;
		alpha = evaluation.MyMath.fitAbsAngleRad(alpha);
		targetG2d.translate(xc, yc);
		targetG2d.rotate(alpha);
		targetG2d.drawString(string, -(currentFontSize * string.length()) / 4, -currentFontSize / 2);
		targetG2d.rotate(-alpha);
		targetG2d.translate(-xc, -yc);
	}

	private float scaleLabelsFont() {
		float size = (float) (scale * SpringShape.fontSize * PhysicalConstants.cm);
		if (size > LABELS_MAX_FONT_SIZE)
			size = LABELS_MAX_FONT_SIZE;
		else if (size < LABELS_MIN_FONT_SIZE)
			size = LABELS_MIN_FONT_SIZE;
		return size;
	}
	
	public float getCurrentFontSize() {
		return currentFontSize;
	}

	private void drawBoundariesOn(Graphics2D targetG2d) {
		targetG2d.setColor(Colors.BOUNDARIES);
		targetG2d.setStroke(arrowStroke);
		Boundaries b = Simulation.getContent().getBoundaries();
		int x1 = coordinateConverter.toScreenX(b.getLeft());
		int y1 = coordinateConverter.toScreenY(b.getUpper());
		int w = coordinateConverter.toScreen(b.getWidth());
		int h = coordinateConverter.toScreen(b.getHeight());
		if (b.isUseBottom())
			targetG2d.drawLine(0, y1 + h, getWidth(), y1 + h);
		if (b.isUseRight())
			targetG2d.drawLine(x1 + w, 0, x1 + w, getHeight());
		if (b.isUseLeft())
			targetG2d.drawLine(x1, 0, x1, getHeight());
		if (b.isUseUpper())
			targetG2d.drawLine(0, y1, getWidth(), y1);
	}

	private void drawCrossOn(Graphics2D targetG2d, double x, double y, boolean drawTag) {
		int xc = coordinateConverter.toScreenX(x);
		int yc = coordinateConverter.toScreenY(y);
		targetG2d.setColor(Colors.CROSS);
		targetG2d.drawLine(xc, yc + 8, xc, yc - 8);
		targetG2d.drawLine(xc - 8, yc, xc + 8, yc);
		targetG2d.setFont(labelsFont);
		targetG2d.setColor(Colors.FONT_TAGS);
		if (drawTag)
			targetG2d.drawString(String.format("(%.1e", x) + String.format("; %.1e) m", y), xc + 4, yc - 4);
	}

	private void drawAxisOn(Graphics2D targetG2d) {
		int x0 = 20;
		int y0 = getHeight() - 20;
		int x1 = x0 + 50;
		int y1 = y0 - 50;
		drawArrowLine(targetG2d, x0, y0, x1, y0, 10, 4);
		drawArrowLine(targetG2d, x0, y0, x0, y1, 10, 4);
		targetG2d.drawString("X", x1, y0 - 4);
		targetG2d.drawString("Y", x0 + 2, y1);
	}

	private void drawScaleLineOn(Graphics2D targetG2d) {
		int l = 50;
		int xc = getWidth() - l / 2 - 20;
		int yc = getHeight() - 20;
		targetG2d.drawLine(xc - l / 2, yc, xc + l / 2, yc);
		targetG2d.drawString(String.format("%.1e m", coordinateConverter.fromScreen(l)), xc - 32, yc - 4);
	}

	public void drawArrowLine(Graphics2D targetG2d, int x1, int y1, Vector v, Color arrowColor, String label) {
		double length = Math.log10(v.norm() + 1) / 2;
		int dx = coordinateConverter.toScreen(length * v.defineCos());
		int dy = coordinateConverter.toScreen(length * v.defineSin());
		if (length > coordinateConverter.fromScreen(ARROW_DRAWING_MIN_THRESHOLD)) {
			targetG2d.setColor(arrowColor);
			targetG2d.setStroke(arrowStroke);
			drawArrowLine(targetG2d, x1, y1, x1 + dx, y1 - dy, 11, 4);
			if (!label.isEmpty()) {
				drawStringTilted(targetG2d, String.format("%.2f " + label, v.norm()), x1, y1, x1 + dx, y1 - dy);
			}
		}
	}

	private static void drawArrowLine(Graphics2D targetG2d, int x1, int y1, int x2, int y2, int d, int h) {
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

		targetG2d.drawLine(x1, y1, x2, y2);
		targetG2d.fillPolygon(xpoints, ypoints, 3);
	}

	public void clearTracksImage() {
		if (drawTracks) {
			drawBackgroundOn((Graphics2D) tracksImage.getGraphics());
		}
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double newTargetScale) {
		targetScale = newTargetScale;
		clearTracksImage();
	}

	public void setCrossX(double crossX) {
		this.crossX = crossX;
	}

	public void setCrossY(double crossY) {
		this.crossY = crossY;
	}

	public double getGridSize() {
		return gridSize;
	}

	public void setGridSize(double size) {
		if (size < 0)
			size = 20 * cm;
		gridSize = size;
		MainWindow.getInstance().refreshGUIControls();
		ConsoleWindow.println(String.format(GUIStrings.GRID_SIZE + " %.2e m", gridSize));
	}

	public boolean isDrawTracks() {
		return drawTracks;
	}

	public void setDrawTracks(boolean b) {
		drawTracks = b;
		clearTracksImage();
		ConsoleWindow.println(GUIStrings.DRAW_TRACKS + ": " + b);
	}

	public boolean isDrawFields() {
		return drawHeatMap;
	}

	public void scaleToAllParticles() {
		if (Simulation.getParticlesCount() > 0) {
			Boundaries b = Simulation.getContent().getBoundaries();
			b.refreshEffectiveBoundaries();
			double h = b.getEffectiveHeight();
			double w = b.getEffectiveWidth();
			setScale(Math.min((getHeight() - AUTOSCALE_MARGIN) / h, (getWidth() - AUTOSCALE_MARGIN) / w));
			System.out.println("viewportH " + getHeight());
			System.out.println("viewportW " + getWidth());
			camera.setFollowing(null);
			camera.setX(b.getEffectiveCenterX());
			camera.setY(b.getEffectiveCenterY());
			ConsoleWindow.println(GUIStrings.AUTOSCALE);
		} else
			scaleToBoundaries();
	}

	public void scaleToBoundaries() {
		Boundaries b = Simulation.getContent().getBoundaries();
		if (!b.isUseLeft() || !b.isUseRight() || !b.isUseBottom())
			scaleToAllParticles();
		else {
			double h = b.getHeight();
			double w = b.getWidth();
			setScale(Math.min((getHeight() - 4) / h, (getWidth() - 2) / w));
			camera.setFollowing(null);
			camera.setX(b.getLeft() + b.getWidth() / 2);
			camera.setY(b.getBottom() + b.getHeight() / 2);
			clearTracksImage();
		}
		ConsoleWindow.println(GUIStrings.AUTOSCALE);
	}

	public MouseMode getMouseMode() {
		return viewportEvent.mouseMode;
	}

	public void setMouseMode(MouseMode mouseMode) {
		viewportEvent.mouseMode = mouseMode;
		Simulation.clearSelection();
		ConsoleWindow.println(GUIStrings.MOUSE_MODE + ": " + viewportEvent.mouseMode);
	}

	public void saveImageToFile() {
		BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D ig2 = buffer.createGraphics();
		drawWholeFrameOn(ig2);
		String fileName = String.format(GUIStrings.SCREENSHOT_NAME + "_%.6fс.jpg", Simulation.getTime());
		try {
			if (javax.imageio.ImageIO.write(buffer, "JPEG", new java.io.File(fileName)))
				ConsoleWindow.println(GUIStrings.IMAGE_SAVED_TO + " " + fileName);
		} catch (IOException e) {
			MainWindow.imageWriteErrorMessage(fileName);
		}
	}

	public Graphics2D getCanvas() {
		return globalCanvas;
	}

	public Camera getCamera() {
		return camera;
	}

	public void reset() {
		refreshLabelsTimer.restart();
		scale = 1e-6;
		setCrossX(0);
		setCrossY(0);
	}
}