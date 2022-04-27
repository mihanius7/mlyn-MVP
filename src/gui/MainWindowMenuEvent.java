package gui;

import static simulation.Simulation.addToSelectionAllParticles;
import static simulation.Simulation.clearSelection;
import static simulation.Simulation.getParticles;
import static simulation.Simulation.getSelectedParticle;
import static simulation.Simulation.getSelectedParticles;
import static simulation.Simulation.setMaxSelectionNumber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import gui.ViewportEvent.MouseMode;
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
		if (src.equals(mainWindowMenu.menuItemOpen)) {
			mainWindow.openSceneDialog();
		} else if (src.equals(mainWindowMenu.menuItemStart)) {
			if (Simulation.getInstance().isActive())
				mainWindow.stopSimulationThread();
			else
				mainWindow.startSimulationThread();
		} else if (src.equals(mainWindowMenu.menuItemStep)) {
			Simulation.perfomStep(50);
		} else if (src.equals(mainWindowMenu.menuItemAutoscale)) {
			Viewport.scaleToAllParticles();
		} else if (src.equals(mainWindowMenu.menuItemViewAll)) {
			Viewport.scaleToBoundaries();
		} else if (src.equals(mainWindowMenu.menuItemExit)) {
			System.exit(0);
		} else if (src.equals(mainWindowMenu.cbMenuItem1pp)) {
			Simulation.interactionProcessor.setUsePPCollisions(mainWindowMenu.cbMenuItem1pp.getState());
		} else if (src.equals(mainWindowMenu.cbMenuItem3)) {
			Simulation.interactionProcessor.setUseExternalForces(mainWindowMenu.cbMenuItem3.getState());
		} else if (src.equals(mainWindowMenu.cbMenuItem3fr)) {
			Simulation.interactionProcessor.setUseFriction(mainWindowMenu.cbMenuItem3fr.getState());
		} else if (src.equals(mainWindowMenu.menuItemFreeze)) {
			getParticles().setZeroVelocities();
		} else if (src.equals(mainWindowMenu.menuItemBoundaries)) {
			MainWindow.showEditBoundariesWindow();
		} else if (src.equals(mainWindowMenu.menuItemSelectAll)) {
			Viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
			mainWindowMenu.rbMenuItemMouse1p.setSelected(true);
			addToSelectionAllParticles();
			mainWindow.setFocusTo(Simulation.getLastAddedParticle());
		} else if (src.equals(mainWindowMenu.cbMenuItem4)) {
			Viewport.drawVelocities = mainWindowMenu.cbMenuItem4.getState();
		} else if (src.equals(mainWindowMenu.cbMenuItem5)) {
			Viewport.drawForces = mainWindowMenu.cbMenuItem5.getState();
		} else if (src.equals(mainWindowMenu.cbMenuItem7)) {
			Viewport.drawGradientParticles = mainWindowMenu.cbMenuItem7.getState();
		} else if (src.equals(mainWindowMenu.cbMenuItem8)) {
			Viewport.drawTags = mainWindowMenu.cbMenuItem8.getState();
		} else if (src.equals(mainWindowMenu.cbMenuItem9)) {
			Viewport.useGrid = mainWindowMenu.cbMenuItem9.getState();
		} else if (src.equals(mainWindowMenu.cbMenuItem10)) {
			Viewport.setDrawTracks(mainWindowMenu.cbMenuItem10.getState());			
		} else if (src.equals(mainWindowMenu.menuItemClear)) {
			Simulation.clearSimulation();
		} else if (src.equals(mainWindowMenu.rbMenuItemMouse1p)) {
			Viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.rbMenuItemMouse1s)) {
			Viewport.setMouseMode(MouseMode.SPRING_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.rbMenuItemMouse2p)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.PARTICLE_ADD);
		} else if (src.equals(mainWindowMenu.rbMenuItemMouse4p)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_COORDINATE);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.rbMenuItemMouse3p)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_ACCELERATION);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.rbMenuItemMouse2s)) {
			clearSelection();
			Viewport.setMouseMode(MouseMode.SPRING_ADD);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.menuItemFixParticle)) {
			getSelectedParticles().fix();
		} else if (src.equals(mainWindowMenu.menuItemColorizeByCharge)) {
			getParticles().colorizeByCharge();
		} else if (src.equals(mainWindowMenu.menuItemCOM)) {
			getSelectedParticles().defineCenterOfMass();
			clearSelection();
		} else if (src.equals(mainWindowMenu.menuItemDelete)) {
			Simulation.removeSelectedParticles();
			Simulation.removeSelectedSprings();
		} else if (src.equals(mainWindowMenu.menuItemSnapToGrid)) {
			getSelectedParticles().snapToGrid();
			Simulation.interactionProcessor.recalculateNeighborsNeeded();
		} else if (src.equals(mainWindowMenu.menuItemFollowParticle)) {
			if (getSelectedParticles().size() > 0) {
				Viewport.camera.setWatchParticle(getSelectedParticle(0));
				clearSelection();
			} else
				MainWindow.NothingIsSelectedMessage();
		} else if (src.equals(mainWindowMenu.menuItemScene2)) {
			Simulation.clearSimulation();
			JOptionPane.showMessageDialog(null, Lang.MENU_MUCH_TIME_NEEDED_PLEASE_WAIT,
					Lang.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
			mainWindow.refreshGUIControls();
		} else if (src.equals(mainWindowMenu.menuItemScene3)) {
			Simulation.clearSimulation();
			mainWindow.refreshGUIControls();
		} else if (src.equals(mainWindowMenu.menuItemAbout)) {
			mainWindow.showAboutWindow();
		} else if (src.equals(mainWindowMenu.menuItemSaveScrn)) {
			mainWindow.saveImageToFile();
		} else
			MainWindow.println("Another menu event");
	}

}
