package gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import calculation.Vector;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.fieldmaps.FieldType;
import gui.shapes.ParticleShape;
import gui.viewport.Viewport;
import gui.viewport.listeners.MouseMode;
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
			Simulation.getInstance().perfomStep(50, true);
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
			Simulation.getInstance().content().getParticles().setZeroVelocities();
		} else if (src.equals(mainWindowMenu.itemBoundaries)) {
			mainWindow.showEditBoundariesWindow();
		} else if (src.equals(mainWindowMenu.itemConsole)) {
			mainWindow.showConsoleWindow();
		} else if (src.equals(mainWindowMenu.itemSelectAll)) {
			viewport.setMouseMode(MouseMode.SELECT_PARTICLE);
			Simulation.getInstance().content().setMaxSelectionNumber(Integer.MAX_VALUE);
			mainWindowMenu.itemMouseSelect2.setSelected(true);
			Simulation.getInstance().content().selectAllParticles();
			mainWindow.setFocusTo(Simulation.getInstance().content().getLastAddedParticle());
		} else if (src.equals(mainWindowMenu.itemVelocities)) {
			ParticleShape.drawVelocities = mainWindowMenu.itemVelocities.getState();
		} else if (src.equals(mainWindowMenu.itemForces)) {
			ParticleShape.drawForces = mainWindowMenu.itemForces.getState();
		} else if (src.equals(mainWindowMenu.itemPretty)) {
			ParticleShape.drawGradientParticles = mainWindowMenu.itemPretty.getState();
		} else if (src.equals(mainWindowMenu.itemTags)) {
			ParticleShape.drawTags = mainWindowMenu.itemTags.getState();
		} else if (src.equals(mainWindowMenu.itemGrid)) {
			viewport.setDrawGrid(mainWindowMenu.itemGrid.getState());
		} else if (src.equals(mainWindowMenu.itemTracks)) {
			viewport.setDrawTracks(mainWindowMenu.itemTracks.getState());			
		} else if (src.equals(mainWindowMenu.itemFields)) {
			viewport.setDrawFieldMap(mainWindowMenu.itemFields.getState(), FieldType.PHYSICAL_FIELD);			
		} else if (src.equals(mainWindowMenu.itemClear)) {
			Simulation.getInstance().clearAll();
			viewport.reset();
			viewport.scaleToBoundaries();
			mainWindow.refreshGUIControls();
		} else if (src.equals(mainWindowMenu.itemMouseSelect2)) {
			viewport.setMouseMode(MouseMode.SELECT_PARTICLE);
		} else if (src.equals(mainWindowMenu.itemMouseSelect1)) {
			viewport.setMouseMode(MouseMode.SELECT_SPRING);
		} else if (src.equals(mainWindowMenu.itemAdd1)) {
			Simulation.getInstance().content().deselectAll();
			viewport.setMouseMode(MouseMode.ADD_PARTICLE);
		} else if (src.equals(mainWindowMenu.itemByPlace)) {
			Simulation.getInstance().content().deselectAll();
			viewport.setMouseMode(MouseMode.PARTICLE_ACT_DISPLACE);
		} else if (src.equals(mainWindowMenu.itemByForce)) {
			Simulation.getInstance().content().deselectAll();
			viewport.setMouseMode(MouseMode.PARTICLE_ACT_FORCE);
		} else if (src.equals(mainWindowMenu.itemAdd2)) {
			Simulation.getInstance().content().deselectAll();
			viewport.setMouseMode(MouseMode.ADD_SPRING);
		} else if (src.equals(mainWindowMenu.itemFix)) {
			Simulation.getInstance().content().getSelectedParticles().fix();
		} else if (src.equals(mainWindowMenu.itemColorizeByCharge)) {
			Simulation.getInstance().content().getParticles().colorizeByCharge();
		} else if (src.equals(mainWindowMenu.itemCoM)) {
			Vector v = Simulation.getInstance().content().getSelectedParticles().defineCenterOfMass();
			viewport.setCrossX(v.X());
			viewport.setCrossY(v.Y());
			Simulation.getInstance().content().deselectAll();
		} else if (src.equals(mainWindowMenu.itemDelete)) {
			Simulation.getInstance().content().removeSelectedParticles();
			Simulation.getInstance().content().removeSelectedSprings();
		} else if (src.equals(mainWindowMenu.itemSnap)) {
			Simulation.getInstance().content().getSelectedParticles().snapToGrid(viewport.getGridSize());
			Simulation.getInstance().interactionProcessor.recalculateNeighborsNeeded();
		} else if (src.equals(mainWindowMenu.itemFollow)) {
			if (Simulation.getInstance().content().getSelectedParticles().size() > 0) {
				viewport.getCamera().setFollowing(Simulation.getInstance().content().getSelectedParticle(0));
				Simulation.getInstance().content().deselectAll();
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
