package gui.viewport.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import gui.MainWindow;
import gui.viewport.Camera;
import gui.viewport.Viewport;
import simulation.Simulation;
import simulation.components.TimeStepController;

public class ViewportKeyListener implements KeyListener {
	
	private Viewport viewport;
	private MainWindow mainWindow;
	
	public ViewportKeyListener(Viewport v, MainWindow w) {
		viewport = v;
		mainWindow = w;
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
		TimeStepController tsh = Simulation.getInstance().timeStepController;
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
	public void keyTyped(KeyEvent arg0) {}

}
