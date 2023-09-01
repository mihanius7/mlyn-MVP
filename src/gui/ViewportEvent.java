package gui;

import static simulation.Simulation.addToSelection;
import static simulation.Simulation.addToSimulation;
import static simulation.Simulation.getSelectedParticle;
import static simulation.Simulation.getSelectedParticles;
import static simulation.Simulation.removeFromSelection;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import elements.force_pair.Spring;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;
import simulation.Simulation;
import simulation.components.TimeStepController;

public class ViewportEvent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	private static final int MAX_SPRINGS_FOR_LABEL_AFTER_SELECTION = 8;
	private int x1, y1, radiusX, radiusY, x0, y0;
	MouseMode mouseMode = MouseMode.PARTICLE_MANIPULATION_ACCELERATION;
	private Viewport viewport;
	private MainWindow mainWindow;

	public ViewportEvent(Viewport v, MainWindow w) {
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
		x0 = arg0.getX();
		y0 = arg0.getY();
		x1 = x0;
		y1 = y0;
		Particle p = null;
		Spring s = null;
		deselectAttachedSprings();
		if (mouseMode == MouseMode.PARTICLE_ADD) {
			p = Simulation.findNearestParticle(CoordinateConverter.fromScreenX(x0), CoordinateConverter.fromScreenY(y0),
					CoordinateConverter.fromScreen(10));
			if (p == null) {
				Simulation.getReferenceParticle().setX(CoordinateConverter.fromScreenX(x0));
				Simulation.getReferenceParticle().setY(CoordinateConverter.fromScreenY(y0));
				if (viewport.useGrid && !Simulation.getInstance().isActive())
					Simulation.getReferenceParticle().snapToGrid(viewport.getGridSize());
				Simulation.getReferenceParticle().setVisible(true);
				mainWindow.clearSelection();
			}
		} else if (mouseMode == MouseMode.PARTICLE_SELECT) {
			p = Simulation.findNearestParticle(CoordinateConverter.fromScreenX(x0), CoordinateConverter.fromScreenY(y0),
					CoordinateConverter.fromScreen(10));
			if (!arg0.isControlDown()) {
				mainWindow.clearSelection();
			}
			if (p != null) {
				if (!p.isSelected()) {
					addToSelection(p);
					mainWindow.setFocusTo(p);
				} else
					removeFromSelection(p);
			} else {
				viewport.setCrossX(CoordinateConverter.fromScreenX(x0));
				viewport.setCrossY(CoordinateConverter.fromScreenY(y0));
			}
		} else if (mouseMode == MouseMode.SPRING_SELECT) {
			s = Simulation.findNearestSpring(CoordinateConverter.fromScreenX(x0), CoordinateConverter.fromScreenY(y0),
					CoordinateConverter.fromScreen(5));
			if (!arg0.isControlDown()) {
				mainWindow.clearSelection();
			}
			if (s != null) {
				if (!s.isSelected()) {
					addToSelection(s);
					mainWindow.setFocusTo(s);
				} else
					removeFromSelection(s);
			}
		} else if (mouseMode == MouseMode.PARTICLE_MANIPULATION_ACCELERATION
				|| mouseMode == MouseMode.PARTICLE_MANIPULATION_COORDINATE || mouseMode == MouseMode.SPRING_ADD) {
			p = Simulation.findNearestParticle(CoordinateConverter.fromScreenX(x0), CoordinateConverter.fromScreenY(y0),
					CoordinateConverter.fromScreen(10));
			if (p != null) {
				addToSelection(p);
				mainWindow.setFocusTo(p);
				radiusX = CoordinateConverter.toScreenX(p.getX()) - x0;
				radiusY = CoordinateConverter.toScreenY(p.getY()) - y0;
				labelAttachedSprings(p);
			}
		}
	}

	private void labelAttachedSprings(Particle p) {
		SpringGroup springGroup = Simulation.findAttachedSprings(p);
		if (springGroup.size() > 1 && springGroup.size() < MAX_SPRINGS_FOR_LABEL_AFTER_SELECTION) {
			for (Spring s1 : springGroup) {
				s1.getShape().setDrawLabel(true);
			}
		}
	}

	private void deselectAttachedSprings() {
		for (Spring s1 : Simulation.getSprings()) {
			s1.getShape().setDrawLabel(false);
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		x1 = arg0.getX();
		y1 = arg0.getY();
		if (mouseMode == MouseMode.PARTICLE_MANIPULATION_COORDINATE && getSelectedParticle(0) != null) {
			if (!Simulation.getInstance().isActive()) {
				getSelectedParticle(0).setX(CoordinateConverter.fromScreenX(x1 + radiusX));
				getSelectedParticle(0).setY(CoordinateConverter.fromScreenY(y1 + radiusY));
				if (viewport.useGrid)
					getSelectedParticle(0).snapToGrid(viewport.getGridSize());
			} else {
				Simulation.interactionProcessor.setParticleTargetXY(new Point2D.Double(
						CoordinateConverter.fromScreenX(x1 + radiusX), CoordinateConverter.fromScreenY(y1 + radiusY)));
				Simulation.interactionProcessor.setMoveToMouse(true);
			}
		} else if (mouseMode == MouseMode.PARTICLE_MANIPULATION_ACCELERATION && getSelectedParticle(0) != null
				&& Simulation.getInstance().isActive()) {
			Simulation.interactionProcessor.setAccelerateByMouse(true);
			Simulation.interactionProcessor.setParticleTargetXY(new Point2D.Double(
					CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1)));
		} else if (mouseMode == MouseMode.PARTICLE_ADD && Simulation.getReferenceParticle().isVisible()) {
			Simulation.getReferenceParticle().setX(CoordinateConverter.fromScreenX(x1));
			Simulation.getReferenceParticle().setY(CoordinateConverter.fromScreenY(y1));
			if (viewport.useGrid && !Simulation.getInstance().isActive())
				Simulation.getReferenceParticle().snapToGrid(viewport.getGridSize());
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		Simulation.interactionProcessor.setMoveToMouse(false);
		Simulation.interactionProcessor.setAccelerateByMouse(false);
		Simulation.interactionProcessor.setParticleTargetXY(new Point2D.Double(0, 0));
		radiusX = 0;
		radiusY = 0;
		if (mouseMode == MouseMode.PARTICLE_ADD && Simulation.getReferenceParticle().isVisible()) {
			Simulation.getReferenceParticle().setVisible(false);
			Particle refP = Simulation.getReferenceParticle();
			Particle newP = new Particle(refP.getX(), refP.getY(), refP);
			addToSimulation(newP);
			addToSelection(newP);
			mainWindow.setFocusTo(newP);
		} else if (mouseMode == MouseMode.SPRING_ADD && getSelectedParticles().size() > 0) {
			Particle p = Simulation.findNearestParticle(CoordinateConverter.fromScreenX(x1),
					CoordinateConverter.fromScreenY(y1), CoordinateConverter.fromScreen(10));
			if (p == null) {
				Particle p2 = new Particle(CoordinateConverter.fromScreenX(x1), CoordinateConverter.fromScreenY(y1), 0,
						0, Simulation.getReferenceParticle().getMass(), Simulation.getReferenceParticle().getRadius());
				addToSimulation(p2);
				p2.setX(CoordinateConverter.fromScreenX(x1));
				p2.setY(CoordinateConverter.fromScreenY(y1));
				if (viewport.useGrid)
					p2.snapToGrid(viewport.getGridSize());
				p = p2;
			}
			if (getSelectedParticle(0) != p) {
				Spring s = new Spring(getSelectedParticle(0), p, 5E4, 0);
				s.setDampingRatio(0.15d);
				s.setResonantFrequency(50d);
				addToSimulation(s);
			}
			mainWindow.clearSelection();
		} else if (mouseMode == MouseMode.PARTICLE_MANIPULATION_ACCELERATION
				|| mouseMode == MouseMode.PARTICLE_MANIPULATION_COORDINATE) {
			mainWindow.clearSelection();
		}
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
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		int kk = arg0.getKeyCode();
		Camera c = viewport.getCamera();
		double s = viewport.getScale();
		if (kk == 75) {
		}
		if (kk == 76) {
		}
		if (kk == 67) {
		}
		if (!arg0.isAltDown() && !arg0.isControlDown() && !arg0.isShiftDown()) {
			if (kk == KeyEvent.VK_RIGHT)
				c.setVx(Camera.CAMERA_KEYBOARD_SPEED / s);
			else if (kk == KeyEvent.VK_LEFT)
				c.setVx(-Camera.CAMERA_KEYBOARD_SPEED / s);
			else if (kk == KeyEvent.VK_UP)
				c.setVy(Camera.CAMERA_KEYBOARD_SPEED / s);
			else if (kk == KeyEvent.VK_DOWN)
				c.setVy(-Camera.CAMERA_KEYBOARD_SPEED / s);
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int kk = arg0.getKeyCode();
		Camera c = viewport.getCamera();
		TimeStepController tsh = Simulation.timeStepController;
		if (kk == KeyEvent.VK_RIGHT)
			c.setVx(0);
		else if (kk == KeyEvent.VK_LEFT)
			c.setVx(0);
		else if (kk == KeyEvent.VK_UP)
			c.setVy(0);
		else if (kk == KeyEvent.VK_DOWN)
			c.setVy(0);
		else if (kk == 46)
			tsh.increaseTimeStepSize(TimeStepController.TIME_STEP_CHANGE_COEFFICIENT);
		else if (kk == 44)
			tsh.decreaseTimeStepSize(TimeStepController.TIME_STEP_CHANGE_COEFFICIENT);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}
}
