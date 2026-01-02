package gui.viewport.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import elements.group.SpringGroup;
import elements.line.Spring;
import elements.point.Particle;
import gui.MainWindow;
import gui.shapes.Crosshair;
import gui.shapes.Meter;
import gui.shapes.Rectangle;
import gui.viewport.Camera;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Simulation;

import static simulation.Simulation.getInstance;

public class ViewportMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

	private static final int MAX_SPRINGS_FOR_LABELING_AFTER_SELECTION = 8;

	protected int x1;
	protected int y1;
	protected int x2;
	protected int y2;

	protected Viewport viewport;
	protected MainWindow mainWindow;

	private Meter meter;
	private Rectangle selectionRectangle;

	public ViewportMouseListener(Viewport v, MainWindow w) {
		viewport = v;
		mainWindow = w;
		getInstance().content().setMaxSelectionNumber(0);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		viewport.updateCrosshair(arg0.getX(), arg0.getY());
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		x1 = arg0.getX();
		y1 = arg0.getY();
		x2 = x1;
		y2 = y1;
		Particle p = null;
		Spring s = null;
		deselectAttachedSprings();
		if (meter != null) {
			viewport.removeShape(meter);
			meter = null;
		}
		if (viewport.getMouseMode() == MouseMode.SELECT_PARTICLE) {
			meter = new Meter();
			viewport.addShape(meter);
			p = Simulation.getInstance().content().getParticles().findNearestParticle(
					CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1),
					CoordinateConverter.fromScreen(10));
			if (!arg0.isControlDown()) {
				mainWindow.clearSelection();
			}
			if (p != null) {
				if (!p.isSelected()) {
					Simulation.getInstance().content().select(p);
					mainWindow.setFocusTo(p);
					labelAttachedSprings(p);
				} else {
					Simulation.getInstance().content().deselect(p);
				}
			}
		} else if (viewport.getMouseMode() == MouseMode.SELECT_SPRING) {
			s = Simulation.getInstance().content().getSprings().findNearestSpring(CoordinateConverter.fromScreenX(x1),
					CoordinateConverter.fromScreenY(y1), CoordinateConverter.fromScreen(5));
			if (!arg0.isControlDown()) {
				mainWindow.clearSelection();
			}
			if (s != null) {
				if (!s.isSelected()) {
					Simulation.getInstance().content().select(s);
					mainWindow.setFocusTo(s);
				} else
					Simulation.getInstance().content().deselect(s);
			}
		}
	}

	protected void labelAttachedSprings(Particle p) {
		SpringGroup springGroup = Simulation.getInstance().content().getSprings().findAttachedSprings(p);
		if (springGroup.size() > 1 && springGroup.size() < MAX_SPRINGS_FOR_LABELING_AFTER_SELECTION) {
			for (Spring s1 : springGroup) {
				s1.getShape().setDrawLabel(true);
			}
		}
	}

	protected void deselectAttachedSprings() {
		for (Spring s1 : Simulation.getInstance().content().getSprings()) {
			s1.getShape().setDrawLabel(false);
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		viewport.updateCrosshair(arg0.getX(), arg0.getY());
		x2 = arg0.getX();
		y2 = arg0.getY();
		if (viewport.getMouseMode() == MouseMode.SELECT_PARTICLE) {
			if (selectionRectangle == null) 
				selectionRectangle = new Rectangle();
			viewport.addShape(selectionRectangle);
			selectionRectangle.setX1(x1);
			selectionRectangle.setY1(y1);
			selectionRectangle.setX2(x2);
			selectionRectangle.setY2(y2);
			Simulation.getInstance().content().getParticles().selectInRect(CoordinateConverter.fromScreenX(x1),
					CoordinateConverter.fromScreenY(y1), CoordinateConverter.fromScreenX(x2),
					CoordinateConverter.fromScreenY(y2));
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		viewport.removeShape(selectionRectangle);
		selectionRectangle = null;
		if (Simulation.getInstance().content().getSelectedParticles().size() >= 2 && arg0.isControlDown()) {
			meter.refresh();
		} else {
			viewport.removeShape(meter);
		}
		if (viewport.getMouseMode() == MouseMode.SELECT_PARTICLE) {
			viewport.setCrossX(CoordinateConverter.fromScreenX(x2));
			viewport.setCrossY(CoordinateConverter.fromScreenY(y2));
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (arg0.isControlDown()) {
			double wh = arg0.getWheelRotation() * Camera.CAMERA_ZOOM_INCREMENT;
			double scale = viewport.getScale();
			if (wh > 0) {
				viewport.setScale(scale / wh);
			} else {
				viewport.setScale(-scale * wh);
				viewport.getCamera().addXWithRollingMean(CoordinateConverter.fromScreenX(arg0.getX()));
				viewport.getCamera().addYWithRollingMean(CoordinateConverter.fromScreenY(arg0.getY()));
			}
		}
		if (meter != null) {
			viewport.removeShape(meter);
			meter = null;
		}
	}

}
