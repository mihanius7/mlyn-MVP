package gui.viewport.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import elements.group.SpringGroup;
import elements.line.Spring;
import elements.point.Particle;
import gui.MainWindow;
import gui.shapes.Meter;
import gui.shapes.Rectangle;
import gui.viewport.Camera;
import gui.viewport.CoordinateConverter;
import gui.viewport.Viewport;
import simulation.Simulation;

public class ViewportMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

	private static final int MAX_SPRINGS_FOR_LABELING_AFTER_SELECTION = 8;
	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private int radiusX;
	private int radiusY;
	public MouseMode mouseMode = MouseMode.SELECT_PARTICLE;
	private Viewport viewport;
	private MainWindow mainWindow;
	private Meter meter;
	private Rectangle rectangle;

	public ViewportMouseListener(Viewport v, MainWindow w) {
		viewport = v;
		mainWindow = w;
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		viewport.grabFocus();
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
		if (mouseMode == MouseMode.ADD_PARTICLE) {
			p = Simulation.getInstance().getContent().getParticles().findNearestParticle(
					CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1),
					CoordinateConverter.fromScreen(10));
			if (p == null) {
				Simulation.getInstance().getContent().getReferenceParticle().setX(CoordinateConverter.fromScreenX(x1));
				Simulation.getInstance().getContent().getReferenceParticle().setY(CoordinateConverter.fromScreenY(y1));
				Simulation.getInstance();
				if (viewport.useGrid && !Simulation.getInstance().isActive())
					Simulation.getInstance().getContent().getReferenceParticle().snapToGrid(viewport.getGridSize());
				Simulation.getInstance().getContent().getReferenceParticle().getShape().setVisible(true);
				mainWindow.clearSelection();
			}
		} else if (mouseMode == MouseMode.SELECT_PARTICLE) {
			meter = new Meter();
			viewport.addShape(meter);
			rectangle = new Rectangle();
			viewport.addShape(rectangle);
			p = Simulation.getInstance().getContent().getParticles().findNearestParticle(
					CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1),
					CoordinateConverter.fromScreen(10));
			if (!arg0.isControlDown()) {
				mainWindow.clearSelection();
			}
			if (p != null) {
				if (!p.isSelected()) {
					Simulation.getInstance().getContent().select(p);
					mainWindow.setFocusTo(p);
					labelAttachedSprings(p);
				} else {
					Simulation.getInstance().getContent().deselect(p);
				}
			} else {
				viewport.setCrossX(CoordinateConverter.fromScreenX(x1));
				viewport.setCrossY(CoordinateConverter.fromScreenY(y1));
			}
		} else if (mouseMode == MouseMode.SELECT_SPRING) {
			s = Simulation.getInstance().getContent().getSprings().findNearestSpring(
					CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1),
					CoordinateConverter.fromScreen(5));
			if (!arg0.isControlDown()) {
				mainWindow.clearSelection();
			}
			if (s != null) {
				if (!s.isSelected()) {
					Simulation.getInstance().getContent().select(s);
					mainWindow.setFocusTo(s);
				} else
					Simulation.getInstance().getContent().deselect(s);
			}
		} else if (mouseMode == MouseMode.PARTICLE_ACT_FORCE
				|| mouseMode == MouseMode.PARTICLE_ACT_DISPLACE || mouseMode == MouseMode.ADD_SPRING) {
			p = Simulation.getInstance().getContent().getParticles().findNearestParticle(
					CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1),
					CoordinateConverter.fromScreen(10));
			if (p != null) {
				Simulation.getInstance().getContent().select(p);
				mainWindow.setFocusTo(p);
				radiusX = CoordinateConverter.toScreenX(p.getX()) - x1;
				radiusY = CoordinateConverter.toScreenY(p.getY()) - y1;
				labelAttachedSprings(p);
			}
		}
	}

	private void labelAttachedSprings(Particle p) {
		SpringGroup springGroup = Simulation.getInstance().getContent().getSprings().findAttachedSprings(p);
		if (springGroup.size() > 1 && springGroup.size() < MAX_SPRINGS_FOR_LABELING_AFTER_SELECTION) {
			for (Spring s1 : springGroup) {
				s1.getShape().setDrawLabel(true);
			}
		}
	}

	private void deselectAttachedSprings() {
		for (Spring s1 : Simulation.getInstance().getContent().getSprings()) {
			s1.getShape().setDrawLabel(false);
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		x2 = arg0.getX();
		y2 = arg0.getY();
		if (mouseMode == MouseMode.PARTICLE_ACT_DISPLACE
				&& Simulation.getInstance().getContent().getSelectedParticle(0) != null) {
			if (!Simulation.getInstance().isActive()) {
				Simulation.getInstance().getContent().getSelectedParticle(0)
						.setX(CoordinateConverter.fromScreenX(x2 + radiusX));
				Simulation.getInstance().getContent().getSelectedParticle(0)
						.setY(CoordinateConverter.fromScreenY(y2 + radiusY));
				if (viewport.useGrid)
					Simulation.getInstance().getContent().getSelectedParticle(0).snapToGrid(viewport.getGridSize());
				Simulation.getInstance().perfomStep(2, false);
			} else {
				Simulation.getInstance().interactionProcessor.setParticleTargetXY(new Point2D.Double(
						CoordinateConverter.fromScreenX(x2 + radiusX), CoordinateConverter.fromScreenY(y2 + radiusY)));
				Simulation.getInstance().interactionProcessor.setMoveToMouse(true);
			}
		} else if (mouseMode == MouseMode.SELECT_PARTICLE) {
			rectangle.setX1(x1);
			rectangle.setY1(y1);
			rectangle.setX2(x2);
			rectangle.setY2(y2);
			Simulation.getInstance().getContent().getParticles().selectInRect(CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1), CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2));
		} else {
			if (mouseMode == MouseMode.PARTICLE_ACT_FORCE
					&& Simulation.getInstance().getContent().getSelectedParticle(0) != null
					&& Simulation.getInstance().isActive()) {
				Simulation.getInstance().interactionProcessor.setAccelerateByMouse(true);
				Simulation.getInstance().interactionProcessor.setParticleTargetXY(
						new Point2D.Double(CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2)));
			} else if (mouseMode == MouseMode.ADD_PARTICLE
					&& Simulation.getInstance().getContent().getReferenceParticle().getShape().isVisible()) {
				Simulation.getInstance().getContent().getReferenceParticle().setX(CoordinateConverter.fromScreenX(x2));
				Simulation.getInstance().getContent().getReferenceParticle().setY(CoordinateConverter.fromScreenY(y2));
				Simulation.getInstance();
				if (viewport.useGrid && !Simulation.getInstance().isActive())
					Simulation.getInstance().getContent().getReferenceParticle().snapToGrid(viewport.getGridSize());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		Simulation.getInstance().interactionProcessor.setMoveToMouse(false);
		Simulation.getInstance().interactionProcessor.setAccelerateByMouse(false);
		Simulation.getInstance().interactionProcessor.setParticleTargetXY(new Point2D.Double(0, 0));
		radiusX = 0;
		radiusY = 0;
		viewport.removeShape(rectangle);
		if (mouseMode == MouseMode.ADD_PARTICLE
				&& Simulation.getInstance().getContent().getReferenceParticle().getShape().isVisible()) {
			Simulation.getInstance().getContent().getReferenceParticle().getShape().setVisible(false);
			Particle refP = Simulation.getInstance().getContent().getReferenceParticle();
			Particle newP = new Particle(refP.getX(), refP.getY(), refP);
			Simulation.getInstance().addToSimulation(newP);
			Simulation.getInstance().getContent().select(newP);
			mainWindow.setFocusTo(newP);
		} else if (mouseMode == MouseMode.ADD_SPRING
				&& Simulation.getInstance().getContent().getSelectedParticles().size() > 0) {
			Particle p = Simulation.getInstance().getContent().getParticles().findNearestParticle(
					CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2),
					CoordinateConverter.fromScreen(10));
			if (p == null) {
				Particle p2 = new Particle(CoordinateConverter.fromScreenX(x2), CoordinateConverter.fromScreenY(y2), 0,
						0, Simulation.getInstance().getContent().getReferenceParticle().getMass(),
						Simulation.getInstance().getContent().getReferenceParticle().getRadius());
				Simulation.getInstance().addToSimulation(p2);
				p2.setX(CoordinateConverter.fromScreenX(x2));
				p2.setY(CoordinateConverter.fromScreenY(y2));
				if (viewport.useGrid)
					p2.snapToGrid(viewport.getGridSize());
				p = p2;
			}
			if (Simulation.getInstance().getContent().getSelectedParticle(0) != p) {
				Spring s = new Spring(Simulation.getInstance().getContent().getSelectedParticle(0), p, 5E4, 0);
				s.setDampingRatio(0.15d);
				s.setResonantFrequency(50d);
				Simulation.getInstance().addToSimulation(s);
			}
			mainWindow.clearSelection();
		} else if (mouseMode == MouseMode.PARTICLE_ACT_FORCE
				|| mouseMode == MouseMode.PARTICLE_ACT_DISPLACE) {
			mainWindow.clearSelection();
		} else if (mouseMode == MouseMode.SELECT_PARTICLE) {
			if (Simulation.getInstance().getContent().getSelectedParticles().size() >= 2 && arg0.isControlDown()) {
				refreshMeter();
			} else {
				viewport.removeShape(meter);
			}
		}
	}

	private void refreshMeter() {
		int size = Simulation.getInstance().getContent().getSelectedParticles().size();
		Particle p1 = Simulation.getInstance().getContent().getSelectedParticles().get(size - 1);
		Particle p2 = Simulation.getInstance().getContent().getSelectedParticles().get(size - 2);
		meter.setX1(CoordinateConverter.toScreenX(p1.getX()));
		meter.setY1(CoordinateConverter.toScreenY(p1.getY()));
		meter.setX2(CoordinateConverter.toScreenX(p2.getX()));
		meter.setY2(CoordinateConverter.toScreenY(p2.getY()));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		double wh = arg0.getWheelRotation() * Camera.CAMERA_ZOOM_INCREMENT;
		double scale = viewport.getScale();		
		if (wh > 0) {
			viewport.setScale(scale / wh);
		} else {
			viewport.setScale(-scale * wh);
			viewport.getCamera().addXWithRollingMean(CoordinateConverter.fromScreenX(arg0.getX()));
			viewport.getCamera().addYWithRollingMean(CoordinateConverter.fromScreenY(arg0.getY()));
		}
		if (meter != null) {
			viewport.removeShape(meter);
			meter = null;
		}
	}

}
