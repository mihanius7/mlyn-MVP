package gui.menu;

import static simulation.Simulation.addToSelectionAllParticles;
import static simulation.Simulation.clearSelection;
import static simulation.Simulation.getParticles;
import static simulation.Simulation.getSelectedParticle;
import static simulation.Simulation.getSelectedParticles;
import static simulation.Simulation.setMaxSelectionNumber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import evaluation.Vector;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.MouseMode;
import gui.Viewport;
import gui.shapes.ParticleShape;
import simulation.Simulation;

public class MainWindowMenuEvent implements ActionListener {

	MainWindow mainWindow;
	MainWindowMenu mainWindowMenu;
	Viewport viewport;

	public MainWindowMenuEvent(MainWindowMenu mwm, Viewport v) {
		mainWindow = MainWindow.getInstance();
		mainWindowMenu = mwm;
		viewport = v;
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
			viewport.scaleToAllParticles();
		} else if (src.equals(mainWindowMenu.itemAutoscale2)) {
			viewport.scaleToBoundaries();
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
			mainWindow.showEditBoundariesWindow();
		} else if (src.equals(mainWindowMenu.itemConsole)) {
			mainWindow.showConsoleWindow();
		} else if (src.equals(mainWindowMenu.itemSelectAll)) {
			viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
			mainWindowMenu.itemMouseSelect2.setSelected(true);
			addToSelectionAllParticles();
			mainWindow.setFocusTo(Simulation.getLastAddedParticle());
		} else if (src.equals(mainWindowMenu.itemVelocities)) {
			ParticleShape.drawVelocities = mainWindowMenu.itemVelocities.getState();
		} else if (src.equals(mainWindowMenu.itemForces)) {
			ParticleShape.drawForces = mainWindowMenu.itemForces.getState();
		} else if (src.equals(mainWindowMenu.itemPretty)) {
			ParticleShape.drawGradientParticles = mainWindowMenu.itemPretty.getState();
		} else if (src.equals(mainWindowMenu.itemTags)) {
			ParticleShape.drawTags = mainWindowMenu.itemTags.getState();
		} else if (src.equals(mainWindowMenu.itemGrid)) {
			viewport.useGrid = mainWindowMenu.itemGrid.getState();
		} else if (src.equals(mainWindowMenu.itemTracks)) {
			viewport.setDrawTracks(mainWindowMenu.itemTracks.getState());			
		} else if (src.equals(mainWindowMenu.itemClear)) {
			Simulation.clearSimulation();
			viewport.reset();
			viewport.scaleToBoundaries();
		} else if (src.equals(mainWindowMenu.itemMouseSelect2)) {
			viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.itemMouseSelect1)) {
			viewport.setMouseMode(MouseMode.SPRING_SELECT);
			setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.itemAdd1)) {
			clearSelection();
			viewport.setMouseMode(MouseMode.PARTICLE_ADD);
		} else if (src.equals(mainWindowMenu.itemByPlace)) {
			clearSelection();
			viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_COORDINATE);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemByForce)) {
			clearSelection();
			viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_ACCELERATION);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemAdd2)) {
			clearSelection();
			viewport.setMouseMode(MouseMode.SPRING_ADD);
			setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemFix)) {
			getSelectedParticles().fix();
		} else if (src.equals(mainWindowMenu.itemColorizeByCharge)) {
			getParticles().colorizeByCharge();
		} else if (src.equals(mainWindowMenu.itemCoM)) {
			Vector v = getSelectedParticles().defineCenterOfMass();
			viewport.setCrossX(v.X());
			viewport.setCrossY(v.Y());
			clearSelection();
		} else if (src.equals(mainWindowMenu.itemDelete)) {
			Simulation.removeSelectedParticles();
			Simulation.removeSelectedSprings();
		} else if (src.equals(mainWindowMenu.itemSnap)) {
			getSelectedParticles().snapToGrid(viewport.getGridSize());
			Simulation.interactionProcessor.recalculateNeighborsNeeded();
		} else if (src.equals(mainWindowMenu.itemFollow)) {
			if (getSelectedParticles().size() > 0) {
				viewport.getCamera().setFollowing(getSelectedParticle(0));
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
			ConsoleWindow.println("Another menu event");
	}

}
