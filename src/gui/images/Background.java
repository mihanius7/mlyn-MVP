package gui.images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gui.viewport.Colors;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;

public class Background {
	private BufferedImage backImage;
	private Viewport viewport;
	private RenderingHints rh;

	public Background(Viewport v) {
		this.viewport = v;
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	public BufferedImage getImage() {
		return backImage;
	}

	public void updateImage() {
		backImage = new BufferedImage(viewport.getWidth(), viewport.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D backCanvas = backImage.createGraphics();
		backCanvas.setRenderingHints(rh);
		if (viewport.isDrawGrid()) {
			double gridStep = viewport.getScale() * viewport.getGridSize();
			int gridMinorStep = (int) gridStep;
			if (gridStep >= 8 && gridStep < viewport.getWidth() * 2) {
				System.out.println("Painting grid... Step " + gridMinorStep + " px.");
				BufferedImage bi = new BufferedImage(gridMinorStep, gridMinorStep, BufferedImage.TYPE_INT_RGB);
				Graphics2D big2d = bi.createGraphics();
				big2d.setRenderingHints(rh);
				big2d.setColor(Colors.BACKGROUND);
				big2d.fillRect(0, 0, gridMinorStep, gridMinorStep);
				big2d.setColor(Colors.GRID);
				big2d.drawLine(0, 0, gridMinorStep, 0);
				big2d.drawLine(0, 0, 0, gridMinorStep);
				TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(CoordinateConverter.toScreenX(0),
						CoordinateConverter.toScreenY(0), gridStep, gridStep));
				backCanvas.setPaint(tp);
			} else {
				backCanvas.setColor(Colors.BACKGROUND);
			}
		} else {
			backCanvas.setColor(Colors.BACKGROUND);
		}
		backCanvas.fillRect(0, 0, viewport.getWidth(), viewport.getHeight());
	}
}
