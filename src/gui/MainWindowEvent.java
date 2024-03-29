package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simulation.Simulation;
import simulation.components.TimeStepController;

public class MainWindowEvent implements ActionListener, ChangeListener, ComponentListener {

	MainWindow mainWindow;

	public MainWindowEvent(MainWindow window) {
		mainWindow = window;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(mainWindow.buttonStart)) {
			if (Simulation.getInstance().isActive())
				mainWindow.stopSimulationThread();
			else
				mainWindow.startSimulationThread();
		} else if (src.equals(mainWindow.buttonTimeStepMode)) {
			Simulation.getInstance().timeStepController.switchMode();
			mainWindow.refreshGUIControls();
		} else if (src.equals(mainWindow.buttonDecrease)) {
			Simulation.getInstance().timeStepController.decreaseTimeStepSize(TimeStepController.TIME_STEP_CHANGE_COEFFICIENT);
		} else if (src.equals(mainWindow.buttonIncrease)) {
			Simulation.getInstance().timeStepController.increaseTimeStepSize(TimeStepController.TIME_STEP_CHANGE_COEFFICIENT);
		} else if (src.equals(mainWindow.buttonRealScale)) {
			Simulation.getInstance().timeStepController.setTimeScale(1);
		} 
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		mainWindow.resizeGUI();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
}