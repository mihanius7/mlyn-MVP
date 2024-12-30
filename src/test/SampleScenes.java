package test;

import static calculation.constants.PhysicalConstants.cm;
import static calculation.constants.PhysicalConstants.kg;

import java.awt.Color;

import elements.point.Particle;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Boundaries;
import simulation.Simulation;
import simulation.components.InteractionType;
import simulation.components.TimeStepController.TimeStepMode;

public class SampleScenes {

	public void initializeScene() {
		scenePreset2();
	}

	public void emptyScene() {
		ConsoleWindow.println(GUIStrings.EMPTY_SCENE_LOADING);
		Simulation.getInstance().timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		Simulation.getInstance().timeStepController.setTimeScale(1d);
		Simulation.getInstance().interactionProcessor.setUseExternalForces(true);
		Simulation.getInstance().interactionProcessor.setUsePPCollisions(true);
		ParticleShape.drawNeighbourRadius = false;
		ParticleShape.drawForces = false;
		ParticleShape.drawTags = false;
		ParticleShape.drawVelocities = false;
		ParticleShape.drawGradientParticles = false;
		Boundaries b = Simulation.getInstance().content().getBoundaries();
		b.setBounds(0, 9.2, 4.2, 0);
		b.setUseAll(true);
		b.setUseUpper(false);
		Simulation.getInstance().perfomStep(10, true);
		MainWindow.setCaption(GUIStrings.NEW_PROJECT_NAME);
		ConsoleWindow.println("	" + GUIStrings.DONE);
	}

	public void scenePreset1() {
		Simulation.getInstance().timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		Simulation.getInstance().timeStepController.setTimeScale(0.001);
		Simulation.getInstance().interactionProcessor.setUsePPCollisions(true);
		Simulation.getInstance().interactionProcessor.setUseExternalForces(false);
		Boundaries b = Simulation.getInstance().content().getBoundaries();
		b.setBounds(0, 9.2, 4.2, 0);
		b.setUseAll(false);
		Simulation.getInstance().add(new Particle(301 * cm, 90 * cm, 1 * kg, -1E-5, 0, 0, 5 * cm, Color.BLUE));
		Simulation.getInstance().add(new Particle(299 * cm, 30 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		Simulation.getInstance().add(new Particle(300 * cm, 120 * cm, 1 * kg, -1E-5, 0, 0, 5 * cm, Color.BLUE));
		Simulation.getInstance().add(new Particle(300 * cm, 10 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		Simulation.getInstance().content().getLastAddedParticle().setMovable(false);
		Simulation.getInstance().add(new Particle(310 * cm, 100 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		Simulation.getInstance().add(new Particle(290 * cm, 100 * cm, 1 * kg, -1E-5, 0, 0, 5 * cm, Color.BLUE));
		Simulation.getInstance().add(new Particle(320 * cm, 130 * cm, 1 * kg, 1E-5, 0, 0, 5 * cm, Color.RED));
		ParticleShape.drawForces = true;
	}

	public void scenePreset2() {
		Simulation.getInstance().timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		Simulation.getInstance().timeStepController.setTimeScale(1E-6);
		Simulation.getInstance().interactionProcessor.setUsePPCollisions(true);
		Simulation.getInstance().interactionProcessor.setUseExternalForces(false);
		Simulation.getInstance().interactionProcessor.setInteractionType(InteractionType.COULOMB);
		Simulation.getInstance().content().getBoundaries().setUseAll(false);
		Simulation.getInstance().add(new Particle(0 * cm, 0 * cm, 0.065 * kg, 2.0E-6, 0, 0, 8 * cm, Color.RED));
		Simulation.getInstance().add(new Particle(60 * cm, 0 * cm, 0.065 * kg, -2.0E-6, 0, 0, 8 * cm, Color.BLUE));
		Simulation.getInstance().add(new Particle(0 * cm, 40 * cm, 0.250 * kg, 4.0E-6, 0, 0, 8 * cm, Color.RED));
		Simulation.getInstance().add(new Particle(60 * cm, 40 * cm, 0.250 * kg, 1.0E-6, 0, 0, 8 * cm, Color.RED));
	}

	public void scenePreset3() {
		Simulation.getInstance().timeStepController.setModeAndReset(TimeStepMode.DYNAMIC);
		Simulation.getInstance().timeStepController.setTimeScale(1);
		Simulation.getInstance().interactionProcessor.setUsePPCollisions(false);
		Simulation.getInstance().interactionProcessor.setUseExternalForces(false);
		Simulation.getInstance().interactionProcessor.setInteractionType(InteractionType.GRAVITATION);
		Simulation.getInstance().content().getBoundaries().setUseAll(false);
		Simulation.getInstance().add(new Particle(0, 0, 5.97E24, 0, 0, 0, 6397000, Color.BLUE));
		Simulation.getInstance().add(new Particle(384400000, 0, 7.35E22, 0, 0, 1022, 1794400, Color.LIGHT_GRAY));
	}

}
