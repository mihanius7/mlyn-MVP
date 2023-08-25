package gui;

import static gui.Viewport.fromScreen;
import static gui.Viewport.fromScreenX;
import static gui.Viewport.fromScreenY;
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

import simulation.Simulation;
import simulation.components.TimeStepController;
import elements.force_pair.Spring;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;

public class ViewportEvent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	public static int x1, y1, dx, dy, x0, y0;
	static MouseMode mouseMode = MouseMode.PARTICLE_MANIPULATION_ACCELERATION;
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
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_ADD) {
			p = Simulation.findNearestParticle(fromScreenX(x0), fromScreenY(y0), fromScreen(10));
			if (p == null) {
				Simulation.getReferenceParticle().setX(fromScreenX(x0));
				Simulation.getReferenceParticle().setY(fromScreenY(y0));
				if (Viewport.useGrid)
					Simulation.getReferenceParticle().snapToGrid();
				Simulation.getReferenceParticle().setVisible(true);
				mainWindow.clearSelection();
			}
		} else if (Viewport.getMouseMode() == MouseMode.PARTICLE_SELECT) {
			p = Simulation.findNearestParticle(fromScreenX(x0), fromScreenY(y0), fromScreen(10));
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
				Viewport.setCrossX(fromScreenX(x0));
				Viewport.setCrossY(fromScreenY(y0));
			}
		} else if (Viewport.getMouseMode() == MouseMode.SPRING_SELECT) {
			s = Simulation.findNearestSpring(fromScreenX(x0), fromScreenY(y0), fromScreen(5));
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
		} else if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_ACCELERATION
				|| Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE
				|| Viewport.getMouseMode() == MouseMode.SPRING_ADD) {
			p = Simulation.findNearestParticle(fromScreenX(x0), fromScreenY(y0), fromScreen(10));
			if (p != null) {
				addToSelection(p);
				mainWindow.setFocusTo(p);
				dx = Viewport.toScreenX(p.getX()) - x0;
				dy = Viewport.toScreenY(p.getY()) - y0;
				labelAttachedSprings(p);
			}
		}
	}

	private void labelAttachedSprings(Particle p) {
		SpringGroup springGroup = Simulation.findAttachedSprings(p);
		if (springGroup.size() > 1) {
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
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE && getSelectedParticle(0) != null) {
			if (!Simulation.getInstance().isActive()) {
				getSelectedParticle(0).setX(fromScreenX(x1 + dx));
				getSelectedParticle(0).setY(fromScreenY(y1 + dy));
				if (Viewport.useGrid)
					getSelectedParticle(0).snapToGrid();
			}
		} else if (Viewport.getMouseMode() == MouseMode.PARTICLE_ADD && Simulation.getReferenceParticle().isVisible()) {
			Simulation.getReferenceParticle().setX(fromScreenX(x1));
			Simulation.getReferenceParticle().setY(fromScreenY(y1));
			if (Viewport.useGrid)
				Simulation.getReferenceParticle().snapToGrid();
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_ADD && Simulation.getReferenceParticle().isVisible()) {
			Simulation.getReferenceParticle().setVisible(false);
			dx = x0 - x1;
			dy = y0 - y1;
			Particle refP = Simulation.getReferenceParticle();
			Particle newP = new Particle(refP.getX(), refP.getY(), refP);
			addToSimulation(newP);
			addToSelection(newP);
			mainWindow.setFocusTo(newP);
		} else if (Viewport.getMouseMode() == MouseMode.SPRING_ADD && getSelectedParticles().size() > 0) {
			Particle p = Simulation.findNearestParticle(fromScreenX(x1), fromScreenY(y1), fromScreen(10));
			if (p == null) {
				Particle p2 = new Particle(Viewport.fromScreenX(x1), fromScreenY(y1), 0, 0,
						Simulation.getReferenceParticle().getMass(), Simulation.getReferenceParticle().getRadius());
				addToSimulation(p2);
				p2.setX(fromScreenX(x1));
				p2.setY(fromScreenY(y1));
				if (Viewport.useGrid)
					p2.snapToGrid();
				p = p2;
			}
			if (getSelectedParticle(0) != p) {
				Spring s = new Spring(getSelectedParticle(0), p, 5E4, 0);
				s.setDampingRatio(0.15d);
				s.setResonantFrequency(50d);
				addToSimulation(s);
			}
			mainWindow.clearSelection();
		} else if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_ACCELERATION
				|| Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE) {
			mainWindow.clearSelection();
		}
		dx = 0;
		dy = 0;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		double wh = arg0.getWheelRotation() * Camera.CAMERA_ZOOM_INCREMENT;
		double scale = Viewport.getScale();
		if (wh > 0) {
			Viewport.setScale(scale / wh);
		} else {
			Viewport.setScale(-scale * wh);
			Viewport.getCamera().addXWithRollingMean(fromScreenX(arg0.getX()));
			Viewport.getCamera().addYWithRollingMean(fromScreenY(arg0.getY()));
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		int kk = arg0.getKeyCode();
		Camera c = Viewport.getCamera();
		double s = Viewport.getScale();
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
		Camera c = Viewport.getCamera();
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
