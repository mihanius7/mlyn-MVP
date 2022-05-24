package gui.menu;

import static simulation.Simulation.addToSelectionAllParticles;
import static simulation.Simulation.clearSelection;
import static simulation.Simulation.getParticles;
import static simulation.Simulation.getSelectedParticle;
import static simulation.Simulation.getSelectedParticles;
import static simulation.Simulation.setMaxSelectionNumber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import gui.MainWindow;
import gui.Viewport;
import gui.ViewportEvent.MouseMode;
import gui.lang.GUIStrings;
import simulation.Simulation;

public class MainWindowMenuEvent implements ActionListener {

	MainWindow mainWindow;
	MainWindowMenu mainWindowMenu;

	public MainWindowMenuEvent(MainWindowMenu mwm) {
		mainWindow = MainWindow.getInstance();
		mainWindowMenu = mwm;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(mainWindowMenu.itemOpen)) {
			mainWindow.openSceneDialog();
		} else if (src.equals(mainWindowMenu.itemStart)) {
			if (Simulation.getInstance().isActive())
				mainWindow.stopSimulationThread();
			else
				mainWindow.startSimulationThread();
		} else if (src.equals(mainWindowMenu.itemSteps)) {
			Simulation.perfomStep(50);
		} else if (src.equals(mainWindowMenu.itemAutoscale1)) {
			Viewport.scaleToAllParticles();
		} else if (src.equals(mainWindowMenu.itemAutoscale2)) {
			Viewport.scaleToBoundaries();
		} else if (src.equals(mainWindowMenu.itemExit)) {
			System.exit(0);
		} else if (src.equals(mainWindowMenu.itemCollisions1)) {
			Simulation.interactionProcessor.setUsePPCollisions(mainWindowMenu.itemCollisions1.getState());
		} else if (src.equals(mainWindowMenu.itemOuterForces)) {
			Simulation.interactionProcessor.setUseExternalForces(mainWindowMenu.itemOuterForces.getState());
		} else if (src.equals(mainWindowMenu.itemFriction)) {
			Simulation.interactionProcessor.setUseFriction(mainWindowMenu.itemFriction.getState());
		} else if (src.equals(mainWindowMenu.itemFreeze)) {
			getParticles().setZeroVelocities();
		} else if (src.equals(mainWindowMenu.itemBoundaries)) {
			MainWindow.showEditBoundariesWindow();
		} else if (src.equals(mainWindowMenu.itemSelectAll)) {
			Viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
			mainWindowMenu.itemMouseSelect2.setSelected(true);
			addToSelectionAllParticles();
			mainWindow.setFocusTo(Simulation.getLastAddedParticle());
		} else if (src.equals(mainWindowMenu.itemVelocities)) {
			Viewport.drawVelocities = mainWindowMenu.itemVelocities.getState();
		} else if (src.equals(mainWindowMenu.itemForces)) {
			Viewport.drawForces = mainWindowMenu.itemForces.getState();
		} else if (src.equals(mainWindowMenu.itemPretty)) {
			Viewport.drawGradientParticles = mainWindowMenu.itemPretty.getState();
		} else if (src.equals(mainWindowMenu.itemTags)) {
			Viewport.drawTags = mainWindowMenu.itemTags.getState();
		} else if (src.equals(mainWindowMenu.itemGrid)) {
			Viewport.useGrid = mainWindowMenu.itemGrid.getState();
		} else if (src.equals(mainWindowMenu.itemTracks)) {
			Viewport.setDrawTracks(mainWindowMenu.itemTracks.getState());			
		} else if (src.equals(mainWindowMenu.itemClear)) {
			Simulation.clearSimulation();
		} else if (src.equals(mainWindowMenu.itemMouseSelect2)) {
			Viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.itemMouseSelect1)) {
			Viewport.setMouseMode(MouseMode.SPRING_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.itemAdd1)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.PARTICLE_ADD);
		} else if (src.equals(mainWindowMenu.itemByPlace)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_COORDINATE);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemByForce)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_ACCELERATION);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemAdd2)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.SPRING_ADD);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemFix)) {
			getSelectedParticles().fix();
		} else if (src.equals(mainWindowMenu.itemColorizeByCharge)) {
			getParticles().colorizeByCharge();
		} else if (src.equals(mainWindowMenu.itemCoM)) {
			getSelectedParticles().defineCenterOfMass();
			clearSelection();
		} else if (src.equals(mainWindowMenu.itemDelete)) {
			Simulation.removeSelectedParticles();
			Simulation.removeSelectedSprings();
		} else if (src.equals(mainWindowMenu.itemSnap)) {
			getSelectedParticles().snapToGrid();
			Simulation.interactionProcessor.recalculateNeighborsNeeded();
		} else if (src.equals(mainWindowMenu.itemFollow)) {
			if (getSelectedParticles().size() > 0) {
				Viewport.camera.setWatchParticle(getSelectedParticle(0));
				clearSelection();
			} else
				MainWindow.NothingIsSelectedMessage();
		} else if (src.equals(mainWindowMenu.itemAbout)) {
			mainWindow.showAboutWindow();
		} else if (src.equals(mainWindowMenu.itemScreenshot)) {
			mainWindow.saveImageToFile();
		} else if (src.equals(mainWindowMenu.itemLanguage)) {
			mainWindow.changeLanguage(mainWindow.askForLanguage());
		} else
			MainWindow.println("Another menu event");
	}

}
