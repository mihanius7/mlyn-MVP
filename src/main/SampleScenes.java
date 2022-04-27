package main;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.kg;
import static constants.PhysicalConstants.m;
import static simulation.Simulation.addParticleQuickly;
import static simulation.Simulation.getLastAddedParticle;
import static simulation.Simulation.interactionProcessor;
import static simulation.Simulation.timeStepController;

import gui.Lang;
import gui.MainWindow;
import gui.Viewport;
import simulation.Simulation;
import simulation.components.Boundaries;
import simulation.components.TimeStepController.TimeStepMode;

public class SampleScenes {

	public void initializeFirstScene() {
		initializeScene2();
		Viewport.scaleToAllParticles();
	}

	public static void emptyScene() {
		timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		timeStepController.setTimeScale(1d);
		interactionProcessor.setUseExternalForces(true);
		interactionProcessor.setUsePPCollisions(true);
		interactionProcessor.setBeta(10 * cm, 10 * cm, 1E7, 0.28);
		Viewport.drawNeighbourRadius = false;
		Viewport.drawForces = false;
		Viewport.drawTags = false;
		Viewport.drawVelocities = false;
		Viewport.drawGradientParticles = false;
		Viewport.useGrid = true;
		Viewport.setGridSize(Viewport.DEFAULT_GRID_SIZE);
		Viewport.setDrawTracks(false);
		Boundaries b = Simulation.getContent().getBoundaries();
		b.setBounds(0, MainWindow.viewportInitWidth * cm, MainWindow.viewportInitHeight * cm, 0);
		b.setUseAll(true);
		b.setUseUpper(false);
		Viewport.scaleToBoundaries();
		Simulation.perfomStep(10);
		MainWindow.setCaption(Lang.NEW_PROJECT_NAME);
	}

public static void initializeScene2() {
		timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		timeStepController.setTimeScale(0.25d);
		interactionProcessor.setUsePPCollisions(true);
		interactionProcessor.setUseExternalForces(true);
		interactionProcessor.setBeta(10 * m, 5 * cm, 1E7, 0.28);		
		addParticleQuickly(301 * cm, 90 * cm, 1 * kg, 5 * cm);
		addParticleQuickly(299 * cm, 30 * cm, 1 * kg, 5 * cm);
		addParticleQuickly(300 * cm, 120 * cm, 1 * kg, 5 * cm);
		addParticleQuickly(300 * cm, 10 * cm, 1000 * kg, 5 * cm);
		getLastAddedParticle().setMovable(false);
	}

}
