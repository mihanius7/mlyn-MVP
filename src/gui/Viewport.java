package gui;

import static constants.PhysicalConstants.cm;
import static simulation.Simulation.interactionProcessor;
import static simulation.Simulation.timeStepController;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import elements.force_pair.Spring;
import elements.groups.ParticleGroup;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;
import evaluation.Vector;
import gui.ViewportEvent.MouseMode;
import gui.lang.GUIStrings;
import simulation.Simulation;
import simulation.components.Boundaries;

public class Viewport extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -8872059106110231269L;
	public static final Color BACKGROUND = new Color(255, 255, 255);
	public static final Color GRID = new Color(200, 200, 200);
	public static final Color BOUNDARIES = new Color(200, 50, 50);
	public static final Color CROSS = Color.MAGENTA;
	public static final Color SELECTED = Color.YELLOW;
	public static final Color PARTICLE_DEFAULT = new Color(100, 100, 100);
	public static final Color PARTICLE_FIXED = Color.BLACK;
	public static final Color PARTICLE_BORDER = Color.DARK_GRAY;
	public static final Color PARTICLE_WATCH = Color.ORANGE;
	public static final Color PARTICLE_CROSS = Color.ORANGE;
	public static final Color SPRING_DEFAULT = new Color(80, 80, 80);
	public static final Color SPRING_OFF = new Color(230, 200, 200);
	public static final Color ARROW_VELOCITY = Color.BLUE;
	public static final Color ARROW_FORCE = Color.ORANGE;
	public static final Color FONT_MAIN = Color.BLACK;
	public static final Color FONT_TAGS = Color.BLACK;
	public static final float SPRING_ZIGZAG_AMPLITUDE = 0.06f;
	public static final int REFRESH_MESSAGES_INTERVAL = 400;
	public static final int FRAME_PAINT_DELAY = 18;
	public static final int AUTOSCALE_MARGIN = 75;
	public static final double DEFAULT_GRID_SIZE = 20 * cm;
	public static boolean drawTags = false, drawMessages = true, drawVelocities = false, drawForces = false,
			drawParticleBorders = true, drawNeighbourRadius = false, useGrid = true, drawGradientParticles = false;
	private static boolean drawTracks = false, drawHeatMap = false;
	private ParticleGroup particles;
	private SpringGroup springs;
	private static Graphics2D globalCanvas, tracksCanvas;
	private static RenderingHints rh;
	private static Font tagFont, mainFont;
	public static final Camera camera = new Camera();
	private static int x, y, viewportHeight, viewportWidth, fps = 0, maxArrowLength_px = 100;
	private static double scale = 100, targetScale = 10, gridSize = DEFAULT_GRID_SIZE, crossX = 0, crossY = 0;
	private static long frameTime, dt;
	private static String timeString = "N/A", timeStepString = "N/A";
	private static Timer refreshLabelsTimer;
	private static BufferedImage tracksImage;
	private BasicStroke particleBorder = new BasicStroke(0.25f);
	private BasicStroke crossStroke = new BasicStroke(3f);
	private BasicStroke arrowStroke = new BasicStroke(2f);

	public Viewport(int initW, int initH) {
		particles = Simulation.getParticles();
		springs = Simulation.getSprings();
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		setBounds(0, 0, initW, initH);
		setDoubleBuffered(true);
		refreshStaticSizeConstants();
		initTracksImage();
		mainFont = new Font("Tahoma", Font.TRUETYPE_FONT, 14);
		tagFont = new Font("Arial", Font.TRUETYPE_FONT, 12);
		refreshLabelsTimer = new Timer(REFRESH_MESSAGES_INTERVAL, this);
	}

	@Override
	public void run() {
		refreshLabelsTimer.start();
		long sleep;
		MainWindow.println(GUIStrings.RENDERING_THREAD_STARTED);
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
		camera.watch();
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
				timeStepString = String
						.format(GUIStrings.TIMESTEP_RESERVE + " > 100 " + GUIStrings.TIME_SCALE + " " + displayedTimeScale, r);
			else
				timeStepString = String
						.format(GUIStrings.TIMESTEP_RESERVE + " = %.1f " + GUIStrings.TIME_SCALE + " " + displayedTimeScale, r);
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

	private void drawCircleOn(Particle p, Graphics2D targetG2d) {
		x = toScreenX(p.getX());
		y = toScreenY(p.getY());
		int r = (int) Math.ceil(scale * p.getRadius());
		if (!drawGradientParticles)
			targetG2d.setPaint(p.getColor());
		else
			targetG2d.setPaint(new GradientPaint(x, y - r, Color.WHITE, x + r, y + r, p.getColor(), false));
		targetG2d.fillOval(x - r, y - r, r * 2, r * 2);
		if (drawTracks) {
			int x0 = toScreenX(p.getLastX());
			int y0 = toScreenY(p.getLastY());
			tracksCanvas.setColor(p.getEigeneColor());
			tracksCanvas.drawLine(x0, y0, x, y);
		}
		if (drawParticleBorders) {
			targetG2d.setColor(PARTICLE_BORDER);
			targetG2d.setStroke(particleBorder);
			targetG2d.drawOval(x - r, y - r, r * 2, r * 2);
		}
	}

	private void drawCirclesOn(Graphics2D targetG2d) {
		Particle p;
		for (int i = 0; i < particles.size(); i++) {
			p = particles.get(i);
			if (p.isVisible()) {
				int r = (int) Math.ceil(scale * p.getRadius());
				drawCircleOn(p, targetG2d);
				if (!p.isMovableX()) {
					targetG2d.setColor(PARTICLE_CROSS);
					targetG2d.setStroke(crossStroke);
					targetG2d.drawLine(x, y + r + 3, x, y - r - 3);
				}
				if (!p.isMovableY()) {
					targetG2d.setColor(PARTICLE_CROSS);
					targetG2d.setStroke(crossStroke);
					targetG2d.drawLine(x - r - 3, y, x + r + 3, y);
				}
				if (drawNeighbourRadius) {
					int nradius = (int) (0.5 * scale * (interactionProcessor.getNeighborRangeExtra()));
					targetG2d.drawOval(x - nradius, y - nradius, nradius * 2, nradius * 2);
				}
				if (drawForces)
					drawArrowLine(targetG2d, x, y, p.getLastForceVector(), ARROW_FORCE);
				if (drawVelocities || p.isSelected())
					drawArrowLine(targetG2d, x, y, p.getVelocityVector(), ARROW_VELOCITY);
				if (drawTags || p.isSelected()) {
					targetG2d.setFont(tagFont);
					targetG2d.setColor(FONT_TAGS);
					x = (int) (toScreenX(p.getX()) + r * 0.707);
					y = (int) (toScreenY(p.getY()) - r * 0.707);
					y -= tagFont.getSize();
					targetG2d.drawString(String.format("#%d: %.1e kg", i, p.getMass()), x, y);
					y += tagFont.getSize();
					targetG2d.drawString(String.format("(%.3f; %.3f) m", p.getX(), p.getY(), p.defineVelocity()), x, y);
					y += tagFont.getSize();
					targetG2d.drawString(String.format("%.3f m/s", p.defineVelocity()), x, y);
				}
			}
		}
		if (Simulation.getReferenceParticle().isVisible())
			drawCircleOn(Simulation.getReferenceParticle(), targetG2d);
		if (camera.getWatchParticle() != null) {
			Particle wp = camera.getWatchParticle();
			x = toScreenX(wp.getX());
			y = toScreenY(wp.getY());
			int r = (int) Math.ceil(scale * wp.getRadius());
			targetG2d.setColor(PARTICLE_WATCH);
			targetG2d.setStroke(new BasicStroke(2f));
			targetG2d.drawOval(x - r, y - r, r * 2, r * 2);
		}
	}

	private void drawSpringsOn(Graphics2D targetG2d) {
		Spring s;
		for (int i = 0; i < springs.size(); i++) {
			s = springs.get(i);
			if (s.isVisible())
				drawSpringOn(s, targetG2d);
		}
		targetG2d.setStroke(new BasicStroke(1f));
	}

	private void drawSpringOn(Spring s, Graphics2D targetG2d) {
		Particle p1 = s.getFirstParticle();
		Particle p2 = s.getSecondParticle();
		int x1 = toScreenX(p1.getX());
		int y1 = toScreenY(p1.getY());
		int x2 = toScreenX(p2.getX());
		int y2 = toScreenY(p2.getY());
		targetG2d.setStroke(
				new BasicStroke((float) (scale * s.getVisibleWidth()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		if (s.isSelected())
			targetG2d.setPaint(s.getColor());
		if (s.isLine())
			targetG2d.drawLine(x1, y1, x2, y2);
		else {
			double alpha = s.defineAngle();
			double beta = alpha + Math.PI / 2;
			double n = s.getNominalLength() / (4 * cm);
			double step = toScreen(s.getDeformatedLength()) / n;
			double width = toScreen(SPRING_ZIGZAG_AMPLITUDE);
			targetG2d.translate(x1, y1);
			targetG2d.rotate(beta);
			int b = 1;
			targetG2d.draw(new Line2D.Double(0, 0, step, width));
			for (int i = 1; i < n - 2; i++) {
				targetG2d.draw(new Line2D.Double(i * step, b * width, (i + 1) * step, -b * width));
				b *= -1;
			}
			targetG2d.draw(new Line2D.Double((n - 1) * step - step / 1.75, b * width, n * step, 0));
			targetG2d.rotate(-beta);
			targetG2d.translate(-x1, -y1);
		}
		if (s.isSelected()) {
			double alpha = s.defineAngle() + Math.PI / 2;
			targetG2d.setFont(tagFont);
			targetG2d.setColor(FONT_TAGS);
			int xc = Math.min(x1, x2) + (Math.max(x1, x2) - Math.min(x1, x2)) / 2;
			int yc = Math.min(y1, y2) + (Math.max(y1, y2) - Math.min(y1, y2)) / 2;
			alpha = evaluation.MyMath.normalizeAbsAngle(alpha);
			targetG2d.translate(xc, yc);
			targetG2d.rotate(alpha);
			targetG2d.drawString(String.format("%.0f Hz", s.getResonantFrequency()), -20, -5);
			targetG2d.rotate(-alpha);
			targetG2d.translate(-xc, -yc);
		}
	}

	private void drawMessagesOn(Graphics2D targetG2d) {
		targetG2d.setColor(Color.BLACK);
		targetG2d.setFont(mainFont);
		targetG2d.drawString(timeString, 2, 12);
		targetG2d.drawString(timeStepString, 2, 28);
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
		targetG2d.setFont(tagFont);
		targetG2d.setColor(FONT_TAGS);
		if (drawTag)
			targetG2d.drawString(String.format("(%.1e", x) + String.format("; %.1e) м", y), xc + 4, yc - 4);
	}

	private void drawAxisOn(Graphics2D targetG2d) {
		int x0 = 20;
		int y0 = viewportHeight - 20;
		int x1 = x0 + 50;
		int y1 = y0 - 50;
		drawArrowLine(targetG2d, x0, y0, x1, y0, 10, 4);
		drawArrowLine(targetG2d, x0, y0, x0, y1, 10, 4);
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

	private void drawArrowLine(Graphics2D g2d, int x1, int y1, Vector v, Color arrowColor) {
		int dx = toScreen(v.X() / 3);
		int dy = toScreen(v.Y() / 3);
		int l = (int) Math.sqrt(dx * dx + dy * dy);
		if (l > 7) {
			if (l > maxArrowLength_px) {
				dx = (int) (maxArrowLength_px * v.defineCos());
				dy = (int) (maxArrowLength_px * v.defineSin());
			}
			g2d.setColor(arrowColor);
			g2d.setStroke(arrowStroke);
			drawArrowLine(g2d, x1, y1, x1 + dx, y1 - dy, 11, 4);
		}
	}

	private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
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

		g.drawLine(x1, y1, x2, y2);
		g.fillPolygon(xpoints, ypoints, 3);
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
		MainWindow.println(String.format(GUIStrings.GRID_SIZE + "%.2e m", gridSize));
	}

	public static boolean isDrawTracks() {
		return drawTracks;
	}

	public static void setDrawTracks(boolean b) {
		drawTracks = b;
		clearTracksImage();
		MainWindow.println(GUIStrings.DRAW_TRACKS + ": " + b);
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
			camera.watchParticle = null;
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
			camera.watchParticle = null;
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
		MainWindow.println(GUIStrings.MOUSE_MODE + ": " + ViewportEvent.mouseMode);
	}

	public void saveImageToFile() {
		BufferedImage buffer = new BufferedImage(viewportWidth, viewportHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D ig2 = buffer.createGraphics();
		drawWholeFrameOn(ig2);
		String fileName = String.format(GUIStrings.SCREENSHOT_NAME + "_%.3fс.jpg", Simulation.getTime());
		try {
			if (javax.imageio.ImageIO.write(buffer, "JPEG", new java.io.File(fileName)))
				MainWindow.println(GUIStrings.IMAGE_SAVED_TO + " " + fileName);
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