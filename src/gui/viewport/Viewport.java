package gui.viewport;

import static calculation.constants.PhysicalConstants.cm;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Timer;

import calculation.Vector;
import calculation.constants.PhysicalConstants;
import elements.Element;
import elements.point.PointMass;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.fieldmaps.RoomModesMap;
import gui.fieldmaps.SPLMap;
import gui.fieldmaps.FieldParameter;
import gui.fieldmaps.FieldType;
import gui.fieldmaps.PhysicalFieldMap;
import gui.fieldmaps.StandingWavesMap;
import gui.images.Background;
import gui.lang.GUIStrings;
import gui.shapes.Crosshair;
import gui.shapes.Shape;
import gui.viewport.listeners.MouseMode;
import gui.viewport.listeners.ViewportKeyListener;
import gui.viewport.listeners.ViewportMouseListener;
import gui.viewport.listeners.ViewportMouseListenersFactory;
import simulation.Boundaries;
import simulation.Simulation;

public class Viewport extends Canvas implements ActionListener, Runnable {

	private static final long serialVersionUID = -1071873059037478882L;
	private static final int SMOOTH_SCALING_STOPING_DIFFERENCE = 4;
	private static final int SMOOTH_SCALING_COEFFICIENT = 2;
	private static final int CROSS_SIZE_PX = 8;
	private static final int SCALE_LINE_MARGIN = 24;
	public static final double ARROW_LENGTH_COEFFICIENT = 0.25;
	public static final int REFRESH_MESSAGES_INTERVAL = 500;
	public final int ARROW_DRAWING_MIN_THRESHOLD = 8;
	public final float LABELS_MIN_FONT_SIZE = 12;
	public final int LABELS_FONT_SIZE = 14;
	public final float LABELS_MAX_FONT_SIZE = 16;
	public final static double DEFAULT_GRID_SIZE = 20 * cm;
	public final int FRAME_PAINT_MIN_DELAY = 17;
	public final int AUTOSCALE_MARGIN = 75;

	Camera camera;
	private BufferedImage frameImage;
	private Graphics2D frameGraphics;
	private BufferedImage tracksImage;
	public Graphics2D tracksFrame;
	private RenderingHints rh;
	private BufferStrategy strategy;

	private static ArrayList<Shape> shapes;
	private static ArrayList<Shape> physicalShapes;
	Boundaries b;

	public Crosshair crosshair;

	private float currentFontSize = LABELS_FONT_SIZE;

	private boolean drawInfo = true;
	public boolean drawGrid = true;
	private boolean drawTracks = false;
	private boolean firstTracksDrawing = true;
	private boolean drawFieldMap = false;
	private boolean scaled;
	private boolean saveScreenshot;
	public Font labelsFont;
	private Font mainFont;
	private int fps = 0;
	private double scale = 100;
	private double targetScale = 10;
	private double gridSize = DEFAULT_GRID_SIZE;
	private double crossX = 0;
	private double crossY = 0;
	private long time;
	private String infoString1 = "N/A", infoString2 = "N/A";
	private Timer refreshLabelsTimer;
	public PhysicalFieldMap fieldMap;
	private FieldType fieldType;
	public Background background;
	private BasicStroke arrowStroke = new BasicStroke(2f);
	public BasicStroke crossStroke = new BasicStroke(3f);
	private MouseMode mouseMode;
	private MainWindow mainWindow;

	public Viewport(int initW, int initH, MainWindow mw) {
		new CoordinateConverter(this);
		shapes = new ArrayList<Shape>();
		physicalShapes = new ArrayList<Shape>();
		mainWindow = mw;
		camera = new Camera(this);
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getDefaultConfiguration();
		frameImage = config.createCompatibleImage(initW, initH);
//		frameImage = new BufferedImage(initW, initH, BufferedImage.TYPE_INT_RGB);
		b = Simulation.getInstance().content().getBoundaries();
		setMouseMode(MouseMode.SELECT_PARTICLE);
		setBounds(0, 0, initW, initH);
		background = new Background(this);
		mainFont = new Font("Tahoma", Font.TRUETYPE_FONT, 14);
		labelsFont = new Font("Arial", Font.TRUETYPE_FONT, LABELS_FONT_SIZE);
		refreshLabelsTimer = new Timer(REFRESH_MESSAGES_INTERVAL, this);
		reset();
	}

	public void paint() {
		do {
			do {
				frameGraphics = (Graphics2D) strategy.getDrawGraphics();
				frameGraphics.drawImage(renderFrame(), 0, 0, null);
				frameGraphics.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
			fps++;
		} while (strategy.contentsLost());
	}

	private Image renderFrame() {
		currentFontSize = scaleLabelsFont();
		frameGraphics = (Graphics2D) frameImage.getGraphics();
		frameGraphics.setRenderingHints(rh);
		if (drawFieldMap && !camera.isFollowing()) {
			fieldMap.updateImage();
			drawBackgroundOn(frameGraphics);
			int x0 = Math.max(0, CoordinateConverter.toScreenX(b.getLeft()));
			int y0 = Math.max(0, CoordinateConverter.toScreenY(b.getUpper()));
			frameGraphics.drawImage(fieldMap.getImage(), x0, y0, null);
		} else if (drawTracks && scaled && !camera.isFollowing()) {
			frameGraphics.drawImage(tracksImage, 0, 0, null);
		} else
			drawBackgroundOn(frameGraphics);
		drawBoundariesOn(frameGraphics);
		drawShapes(frameGraphics);
		if (Simulation.getInstance().content().getReferenceParticle().getShape().isVisible())
			Simulation.getInstance().content().getReferenceParticle().getShape().paintShape(frameGraphics, this);
		if (camera.getFollowing() != null) {
			Element following = camera.getFollowing();
			drawCrossOn(frameGraphics, following.getCenterPoint().x, following.getCenterPoint().y, false);
		}
		frameGraphics.setColor(Colors.FONT_TAGS);
		frameGraphics.setStroke(arrowStroke);
		drawCrossOn(frameGraphics, crossX, crossY, false);
		drawAxisOn(frameGraphics);
		drawScaleLineOn(frameGraphics);
		if (drawInfo)
			drawInfoStringsOn(frameGraphics);
		if (saveScreenshot) {
			saveScreenshot();
			saveScreenshot = false;
		}
		return frameImage;
	}

	@Override
	public void run() {
		refreshLabelsTimer.start();
		ConsoleWindow.println(GUIStrings.RENDERING_THREAD_STARTED);
		strategy = getBufferStrategy();
		while (true) {
			time = System.currentTimeMillis();
			checkShapesList();
			scaleSmooth();
			camera.follow();
			paint();
			long renderingTime = System.currentTimeMillis() - time;
			waitForNextRender(renderingTime);
		}
	}

	private void waitForNextRender(long renderingTime) {
		long sleep;
		sleep = FRAME_PAINT_MIN_DELAY - renderingTime;
		if (sleep < 0)
			sleep = FRAME_PAINT_MIN_DELAY;
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			System.out.println(GUIStrings.INTERRUPTED_THREAD + ": " + e.getMessage());
		}
	}

	private void checkShapesList() {
		if (physicalShapes.size() != Simulation.getInstance().content().getParticlesCount()
				+ Simulation.getInstance().content().getSpringsCount()) {
			shapes.removeAll(physicalShapes);
			physicalShapes.clear();
			for (int i = 0; i < Simulation.getInstance().content().getParticlesCount(); i++) {
				if (Simulation.getInstance().content().particle(i) != null)
					physicalShapes.add(Simulation.getInstance().content().particle(i).getShape());
			}
			for (int i = 0; i < Simulation.getInstance().content().getSpringsCount(); i++) {
				if (Simulation.getInstance().content().getSpring(i) != null)
					physicalShapes.add(Simulation.getInstance().content().getSpring(i).getShape());
			}
			shapes.addAll(physicalShapes);
		}
	}

	public synchronized boolean addShape(Shape s) {
		boolean result = false;
		if (!shapes.contains(s)) {
			shapes.add(s);
			result = true;
		}
		return result;
	}

	public synchronized boolean removeShape(Shape s) {
		return shapes.remove(s);
	}

	public synchronized boolean isContainsShape(Shape s) {
		return shapes.contains(s);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == refreshLabelsTimer) {
			updateInfoStrings();
			MainWindow.getInstance().refreshGUIDisplays();
			Simulation.getInstance().timeStepController.clearStepsPerSecond();
			fps = 0;
		}
	}

	private void updateInfoStrings() {
		Simulation.getInstance().timeStepController.updateTimeScale();
		double r = Simulation.getInstance().interactionProcessor.getTimeStepReserveRatio();
		double timeScale = Simulation.getInstance().timeStepController.getMeasuredTimeScale();
		String displayedTimeScale = "undefined";
		infoString1 = String.format("t = %.3f c, ", Simulation.getInstance().time())
				+ String.format("dt = %.4f", Simulation.getInstance().timeStepController.getTimeStepSize() * 1000)
				+ " ms, " + String.format("Vmax = %.2f m/s", PointMass.maxVelocity) + ", fps = "
				+ fps * 1000.0 / REFRESH_MESSAGES_INTERVAL + ", sps = "
				+ Simulation.getInstance().timeStepController.getStepsPerSecond() / (REFRESH_MESSAGES_INTERVAL / 1000.0)
						/ 1000.0
				+ "k";
		if (Simulation.getInstance().isActive())
			if (timeScale > 1000 || timeScale < 1e-3)
				displayedTimeScale = String.format("%.2e", timeScale);
			else
				displayedTimeScale = String.format("%.3f", timeScale);
		if (r > 100)
			infoString2 = String.format(
					GUIStrings.TIMESTEP_RESERVE + " > 100 " + GUIStrings.TIME_SCALE + " " + displayedTimeScale, r);
		else
			infoString2 = String.format(
					GUIStrings.TIMESTEP_RESERVE + " = %.1f " + GUIStrings.TIME_SCALE + " " + displayedTimeScale, r);
		infoString2 = infoString2
				.concat(", interactions: " + Simulation.getInstance().interactionProcessor.getPairInteractionCount());
		infoString2 = infoString2.concat(", srchs skipped: "
				+ Simulation.getInstance().interactionProcessor.getNeighborSearchsSkipStepsNumber());
	}

	void drawBackgroundOn(Graphics2D targetG2d) {
		targetG2d.drawImage(background.getImage(), 0, 0, null);
	}

	private void drawShapes(Graphics2D targetG2d) {
		Shape shape;
		for (int i = 0; i < shapes.size(); i++) {
			shape = shapes.get(i);
			if (shape.isVisible()) {
				shape.paintShape(targetG2d, this);
			}
		}
		firstTracksDrawing = false;
	}

	private void drawInfoStringsOn(Graphics2D targetG2d) {
		targetG2d.setColor(getMainFontColor());
		targetG2d.setFont(mainFont);
		targetG2d.drawString(infoString1, 2, 12);
		targetG2d.drawString(infoString2, 2, 28);
	}

	public Color getMainFontColor() {
		return drawFieldMap ? Color.DARK_GRAY : Colors.FONT_MAIN;
	}

	public void drawStringTilted(Graphics2D targetG2d, String string, int x1, int y1, int x2, int y2) {
		double alpha = Math.atan2(x2 - x1, y1 - y2) + Math.PI / 2;
		targetG2d.setFont(labelsFont.deriveFont(getCurrentFontSize()));
		targetG2d.setColor(getMainFontColor());
		int xc = Math.min(x1, x2) + (Math.max(x1, x2) - Math.min(x1, x2)) / 2;
		int yc = Math.min(y1, y2) + (Math.max(y1, y2) - Math.min(y1, y2)) / 2;
		alpha = calculation.Functions.fitAbsAngleRad(alpha);
		targetG2d.translate(xc, yc);
		targetG2d.rotate(alpha);
		targetG2d.drawString(string, -(currentFontSize * string.length()) / 4, -currentFontSize / 2);
		targetG2d.rotate(-alpha);
		targetG2d.translate(-xc, -yc);
	}

	private float scaleLabelsFont() {
		float size = (float) (scale * LABELS_FONT_SIZE * PhysicalConstants.cm);
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
		int x1 = CoordinateConverter.toScreenX(b.getLeft());
		int y1 = CoordinateConverter.toScreenY(b.getUpper());
		int w = CoordinateConverter.toScreen(b.getWidth());
		int h = CoordinateConverter.toScreen(b.getHeight());
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
		int xc = CoordinateConverter.toScreenX(x);
		int yc = CoordinateConverter.toScreenY(y);
		targetG2d.setColor(Colors.CROSS);
		targetG2d.drawLine(xc, yc + CROSS_SIZE_PX, xc, yc - CROSS_SIZE_PX);
		targetG2d.drawLine(xc - CROSS_SIZE_PX, yc, xc + CROSS_SIZE_PX, yc);
		targetG2d.setColor(getMainFontColor());
		targetG2d.setFont(labelsFont);
		targetG2d.setColor(Colors.CROSS);
		if (drawTag)
			targetG2d.drawString(String.format("(%.2e", x) + String.format("; %.2e) m", y), xc + 4, yc - 4);
	}

	private void drawAxisOn(Graphics2D targetG2d) {
		int x0 = SCALE_LINE_MARGIN;
		int y0 = getHeight() - SCALE_LINE_MARGIN;
		int x1 = x0 + 50;
		int y1 = y0 - 50;
		targetG2d.setColor(getMainFontColor());
		drawArrowLine(targetG2d, x0, y0, x1, y0, 10, 4);
		drawArrowLine(targetG2d, x0, y0, x0, y1, 10, 4);
		targetG2d.drawString("X", x1, y0 - 4);
		targetG2d.drawString("Y", x0 + 2, y1);
	}

	private void drawScaleLineOn(Graphics2D targetG2d) {
		targetG2d.setColor(getMainFontColor());
		int l = 50;
		targetG2d.drawLine(getWidth() - l - SCALE_LINE_MARGIN, getHeight() - SCALE_LINE_MARGIN,
				getWidth() - SCALE_LINE_MARGIN, getHeight() - SCALE_LINE_MARGIN);
		targetG2d.drawString(String.format("%.1e m", CoordinateConverter.fromScreen(l)),
				getWidth() - l - SCALE_LINE_MARGIN, getHeight() - SCALE_LINE_MARGIN - 4);
	}

	public void drawArrowLine(Graphics2D targetG2d, int x1, int y1, Vector v, Color arrowColor, String label) {
		double length = Math.log10(v.norm() + 1) * ARROW_LENGTH_COEFFICIENT;
		int dx = CoordinateConverter.toScreen(length * v.defineCos());
		int dy = CoordinateConverter.toScreen(length * v.defineSin());
		if (length > CoordinateConverter.fromScreen(ARROW_DRAWING_MIN_THRESHOLD)) {
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

	private void scaleSmooth() {
		if (!scaled) {
			if (Math.abs(targetScale - scale) >= SMOOTH_SCALING_STOPING_DIFFERENCE) {
				scale += (targetScale - scale) / SMOOTH_SCALING_COEFFICIENT;
			} else {
				scale = targetScale;
				scaled = true;
				initBackgroundImage();
				initFieldMapImage();
			}
		}
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double newTargetScale) {
		targetScale = newTargetScale;
		scaled = false;
		firstTracksDrawing = true;
		clearTracksImage();
	}

	public boolean isScaled() {
		return scaled;
	}

	public void setScaledToFalse() {
		scaled = false;
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
			size = 5 * cm;
		gridSize = size;
		MainWindow.getInstance().refreshGUIControls();
		ConsoleWindow.println(String.format(GUIStrings.GRID_SIZE + " %.2e m", gridSize));
	}

	public boolean isDrawTracks() {
		return drawTracks;
	}

	public boolean isDrawFields() {
		return drawFieldMap;
	}

	public void setDrawTracks(boolean b) {
		drawTracks = b;
		if (drawTracks) {
			initTracksImage();
		}
		ConsoleWindow.println(GUIStrings.DRAW_TRACKS + ": " + b);
	}

	public void resizeFrameImage() {
		frameImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	}

	public void initTracksImage() {
		tracksImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		tracksFrame = tracksImage.createGraphics();
		tracksFrame.setStroke(new BasicStroke(2f));
		tracksFrame.setRenderingHints(rh);
		clearTracksImage();
	}

	public void clearTracksImage() {
		if (drawTracks) {
			firstTracksDrawing = true;
			drawBackgroundOn((Graphics2D) tracksImage.getGraphics());
		}
	}

	public void setDrawFieldMap(boolean b, FieldType type) {
		this.fieldType = type;
		drawFieldMap = b;
		initFieldMapImage();
		setDrawCrosshair(b);
		setDrawTracks(false);
	}

	public boolean isDrawFieldMap() {
		return drawFieldMap;
	}

	public void initFieldMapImage() {
		if (drawFieldMap) {
			switch (this.fieldType) {
			case SPL_MAP:
				fieldMap = new SPLMap(this);
				break;
			case STANDING_WAVES:
				fieldMap = new StandingWavesMap(this);
				break;
			case ROOM_MODES:
				fieldMap = new RoomModesMap(this);
				break;
			default:
				fieldMap = new PhysicalFieldMap(this);
				break;
			}
		}
	}

	public void setDrawGrid(boolean b) {
		drawGrid = b;
		initBackgroundImage();
	}

	public boolean isDrawGrid() {
		return drawGrid && !camera.isFollowing();
	}

	public void initBackgroundImage() {
		background.updateImage();
	}

	public void setDrawCrosshair(boolean b) {
		if (b) {
			crosshair = new Crosshair();
			shapes.add(crosshair);
		} else {
			shapes.remove(crosshair);
			crosshair = null;
		}
	}

	public void scaleToAllParticles() {
		if (Simulation.getInstance().content().getParticlesCount() > 0) {
			Boundaries b = Simulation.getInstance().content().getBoundaries();
			b.refreshContentBoundaries();
			double h = b.getContentHeight();
			double w = b.getContentWidth();
			setScale(Math.min((getHeight() - AUTOSCALE_MARGIN) / h, (getWidth() - AUTOSCALE_MARGIN) / w));
			System.out.println("viewportH " + getHeight());
			System.out.println("viewportW " + getWidth());
			camera.setFollowing(null);
			camera.setX(b.getContentCenterX());
			camera.setY(b.getContentCenterY());
			ConsoleWindow.println(GUIStrings.AUTOSCALE);
		} else
			scaleToBoundaries();
	}

	public void scaleToBoundaries() {
		Boundaries b = Simulation.getInstance().content().getBoundaries();
		if (!b.isUseRight() || !b.isUseUpper())
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
		return mouseMode;
	}

	public void setMouseMode(MouseMode newMouseMode) {
		addKeyListener(new ViewportKeyListener(this, mainWindow));
		ViewportMouseListener mouseListener = ViewportMouseListenersFactory.getViewportListener(newMouseMode, this,
				mainWindow);
		if (getMouseListeners().length > 0)
			removeMouseListener(getMouseListeners()[0]);
		if (getMouseMotionListeners().length > 0)
			removeMouseMotionListener(getMouseMotionListeners()[0]);
		if (getMouseWheelListeners().length > 0)
			removeMouseWheelListener(getMouseWheelListeners()[0]);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
		Simulation.getInstance().content().deselectAll();
		mouseMode = newMouseMode;
		ConsoleWindow.println(GUIStrings.MOUSE_MODE + ": " + mouseMode);
	}

	public void prepareScreenshotAndSave() {
		saveScreenshot = true;
	}

	private void saveScreenshot() {
		String fileName = String.format(GUIStrings.SCREENSHOT_NAME + "_%.6fs.png", Simulation.getInstance().time());
		try {
			if (javax.imageio.ImageIO.write(frameImage, "png", new java.io.File(fileName)))
				ConsoleWindow.println(GUIStrings.IMAGE_SAVED_TO + " " + fileName);
		} catch (IOException e) {
			MainWindow.imageWriteErrorMessage(fileName);
		}
	}

	public BufferedImage getFrameImage() {
		return frameImage;
	}

	public Camera getCamera() {
		return camera;
	}

	public void reset() {
		refreshLabelsTimer.restart();
		scale = 1e-3;
		setCrossX(0);
		setCrossY(0);
		setDrawGrid(true);
		setDrawFieldMap(false, fieldType);
		initTracksImage();
	}

	public void updateCrosshair(int x, int y) {
		if (crosshair != null) {
			crosshair.setX(x);
			crosshair.setY(y);
		}
	}

	public boolean isFirstTracksDrawing() {
		return firstTracksDrawing;
	}
}