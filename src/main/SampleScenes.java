package main;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.kg;
import static constants.PhysicalConstants.m;
import static simulation.Simulation.getLastAddedParticle;
import static simulation.Simulation.interactionProcessor;
import static simulation.Simulation.timeStepController;

import elements.point_mass.Particle;
import gui.MainWindow;
import gui.Viewport;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Simulation;
import simulation.components.Boundaries;
import simulation.components.TimeStepController.TimeStepMode;

public class SampleScenes {

	public void initializeScene() {
		scenePreset();
	}

	public static void emptyScene() {
		timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		timeStepController.setTimeScale(1d);
		interactionProcessor.setUseExternalForces(true);
		interactionProcessor.setUsePPCollisions(true);
		interactionProcessor.setBeta(10 * cm, 10 * cm, 1E7, 0.28);
		ParticleShape.drawNeighbourRadius = false;
		ParticleShape.drawForces = false;
		ParticleShape.drawTags = false;
		ParticleShape.drawVelocities = false;
		ParticleShape.drawGradientParticles = false;
		Viewport.useGrid = true;
		Viewport.setGridSize(Viewport.DEFAULT_GRID_SIZE);
		Viewport.setDrawTracks(false);
		Boundaries b = Simulation.getContent().getBoundaries();
		b.setBounds(0, 9.2, 4.2, 0);
		b.setUseAll(true);
		b.setUseUpper(false);
		Simulation.perfomStep(10);
		MainWindow.setCaption(GUIStrings.NEW_PROJECT_NAME);
		Viewport.scaleToBoundaries();
	}

public static void scenePreset() {
		timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		timeStepController.setTimeScale(0.25d);
		interactionProcessor.setUsePPCollisions(true);
		interactionProcessor.setUseExternalForces(true);
		interactionProcessor.setBeta(10 * m, 5 * cm, 1E7, 0.28);		
		Boundaries b = Simulation.getContent().getBoundaries();
		b.setBounds(0, 9.2, 4.2, 0);
		Simulation.addToSimulation(new Particle(301 * cm, 90 * cm, 1 * kg, 5 * cm));
		Simulation.addToSimulation(new Particle(299 * cm, 30 * cm, 1 * kg, 5 * cm));
		Simulation.addToSimulation(new Particle(300 * cm, 120 * cm, 1 * kg, 5 * cm));
		Simulation.addToSimulation(new Particle(300 * cm, 10 * cm, 1000 * kg, 5 * cm));
		getLastAddedParticle().setMovable(false);
		Viewport.scaleToBoundaries();
	}

}
