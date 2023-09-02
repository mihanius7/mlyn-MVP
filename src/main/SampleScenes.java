package main;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.kg;
import static constants.PhysicalConstants.m;
import static simulation.Simulation.getLastAddedParticle;
import static simulation.Simulation.interactionProcessor;
import static simulation.Simulation.timeStepController;

import java.awt.Color;

import elements.Boundaries;
import elements.point_mass.Particle;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Simulation;
import simulation.components.TimeStepController.TimeStepMode;

public class SampleScenes {

	public void initializeScene() {
		scenePreset();
	}

	public void emptyScene() {
		ConsoleWindow.println(GUIStrings.EMPTY_SCENE_LOADING);
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
		Boundaries b = Simulation.getContent().getBoundaries();
		b.setBounds(0, 9.2, 4.2, 0);
		b.setUseAll(true);
		b.setUseUpper(false);
		Simulation.perfomStep(10);
		MainWindow.setCaption(GUIStrings.NEW_PROJECT_NAME);
		ConsoleWindow.println("	"+ GUIStrings.DONE);
	}

public void scenePreset() {
		timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		timeStepController.setTimeScale(0.25d);
		interactionProcessor.setUsePPCollisions(true);
		interactionProcessor.setUseExternalForces(false);
		interactionProcessor.setBeta(10 * m, 5 * cm, 1E7, 0.28);
		Boundaries b = Simulation.getContent().getBoundaries();
		b.setBounds(0, 9.2, 4.2, 0);
		Simulation.addToSimulation(new Particle(301 * cm, 90 * cm, 1 * kg, -1E-5, 0, 0, 5 * cm, Color.BLUE));
		Simulation.addToSimulation(new Particle(299 * cm, 30 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		Simulation.addToSimulation(new Particle(300 * cm, 120 * cm, 1 * kg, -1E-5, 0, 0, 5 * cm, Color.BLUE));
		Simulation.addToSimulation(new Particle(300 * cm, 10 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		getLastAddedParticle().setMovable(false);
		Simulation.addToSimulation(new Particle(310 * cm, 100 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		Simulation.addToSimulation(new Particle(290 * cm, 100 * cm, 1 * kg, -1E-5, 0, 0, 5 * cm, Color.BLUE));
		Simulation.addToSimulation(new Particle(320 * cm, 130 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		ParticleShape.drawForces = true;
	}

}
