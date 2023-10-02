package gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gui.ConsoleWindow;
import gui.MainWindow;
import gui.shapes.ParticleShape;
import gui.viewport.MouseMode;
import gui.viewport.Viewport;
import simulation.Simulation;
import simulation.math.Vector;

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
			Simulation.getInstance().perfomStep(50);
		} else if (src.equals(mainWindowMenu.itemAutoscale1)) {
			viewport.scaleToAllParticles();
		} else if (src.equals(mainWindowMenu.itemAutoscale2)) {
			viewport.scaleToBoundaries();
		} else if (src.equals(mainWindowMenu.itemExit)) {
			System.exit(0);
		} else if (src.equals(mainWindowMenu.itemCollisions1)) {
			Simulation.getInstance().interactionProcessor.setUsePPCollisions(mainWindowMenu.itemCollisions1.getState());
		} else if (src.equals(mainWindowMenu.itemOuterForces)) {
			Simulation.getInstance().interactionProcessor.setUseExternalForces(mainWindowMenu.itemOuterForces.getState());
		} else if (src.equals(mainWindowMenu.itemFriction)) {
			Simulation.getInstance().interactionProcessor.setUseFriction(mainWindowMenu.itemFriction.getState());
		} else if (src.equals(mainWindowMenu.itemFreeze)) {
			Simulation.getInstance().getContent().getParticles().setZeroVelocities();
		} else if (src.equals(mainWindowMenu.itemBoundaries)) {
			mainWindow.showEditBoundariesWindow();
		} else if (src.equals(mainWindowMenu.itemConsole)) {
			mainWindow.showConsoleWindow();
		} else if (src.equals(mainWindowMenu.itemSelectAll)) {
			viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			Simulation.getInstance().getContent().setMaxSelectionNumber(Integer.MAX_VALUE);
			mainWindowMenu.itemMouseSelect2.setSelected(true);
			Simulation.getInstance().getContent().selectAllParticles();
			mainWindow.setFocusTo(Simulation.getInstance().getContent().getLastAddedParticle());
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
			Simulation.getInstance().clearSimulation();
			viewport.reset();
			viewport.scaleToBoundaries();
		} else if (src.equals(mainWindowMenu.itemMouseSelect2)) {
			viewport.setMouseMode(MouseMode.PARTICLE_SELECT);
			Simulation.getInstance().getContent().setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.itemMouseSelect1)) {
			viewport.setMouseMode(MouseMode.SPRING_SELECT);
			Simulation.getInstance().getContent().setMaxSelectionNumber(Integer.MAX_VALUE);
		} else if (src.equals(mainWindowMenu.itemAdd1)) {
			Simulation.getInstance().getContent().deselectAll();
			viewport.setMouseMode(MouseMode.PARTICLE_ADD);
		} else if (src.equals(mainWindowMenu.itemByPlace)) {
			Simulation.getInstance().getContent().deselectAll();
			viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_COORDINATE);
			Simulation.getInstance().getContent().setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemByForce)) {
			Simulation.getInstance().getContent().deselectAll();
			viewport.setMouseMode(MouseMode.PARTICLE_MANIPULATION_ACCELERATION);
			Simulation.getInstance().getContent().setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemAdd2)) {
			Simulation.getInstance().getContent().deselectAll();
			viewport.setMouseMode(MouseMode.SPRING_ADD);
			Simulation.getInstance().getContent().setMaxSelectionNumber(1);
		} else if (src.equals(mainWindowMenu.itemFix)) {
			Simulation.getInstance().getContent().getSelectedParticles().fix();
		} else if (src.equals(mainWindowMenu.itemColorizeByCharge)) {
			Simulation.getInstance().getContent().getParticles().colorizeByCharge();
		} else if (src.equals(mainWindowMenu.itemCoM)) {
			Vector v = Simulation.getInstance().getContent().getSelectedParticles().defineCenterOfMass();
			viewport.setCrossX(v.X());
			viewport.setCrossY(v.Y());
			Simulation.getInstance().getContent().deselectAll();
		} else if (src.equals(mainWindowMenu.itemDelete)) {
			Simulation.getInstance().getContent().removeSelectedParticles();
			Simulation.getInstance().getContent().removeSelectedSprings();
		} else if (src.equals(mainWindowMenu.itemSnap)) {
			Simulation.getInstance().getContent().getSelectedParticles().snapToGrid(viewport.getGridSize());
			Simulation.getInstance().interactionProcessor.recalculateNeighborsNeeded();
		} else if (src.equals(mainWindowMenu.itemFollow)) {
			if (Simulation.getInstance().getContent().getSelectedParticles().size() > 0) {
				viewport.getCamera().setFollowing(Simulation.getInstance().getContent().getSelectedParticle(0));
				Simulation.getInstance().getContent().deselectAll();
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
