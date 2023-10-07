package simulation;

import java.util.ArrayList;

import elements.group.ParticleGroup;
import elements.group.SpringGroup;
import elements.line.Spring;
import elements.point.Particle;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.lang.GUIStrings;
import simulation.components.InteractionProcessor;
import simulation.components.SimulationComponent;
import simulation.components.TimeStepController;
import test.SampleScenes;

public class Simulation implements Runnable {

	private static Simulation instance;

	private SimulationContent content;

	private final ParticleGroup pForRemove = new ParticleGroup();
	private final ParticleGroup pForAdd = new ParticleGroup();
	private final SpringGroup sForRemove = new SpringGroup();
	private final SpringGroup sForAdd = new SpringGroup();

	private final ArrayList<SimulationComponent> simulationComponents = new ArrayList<SimulationComponent>();

	public InteractionProcessor interactionProcessor;
	public TimeStepController timeStepController;

	private double time = 0;
	private double stopTime = Double.MAX_VALUE;
	private long stepEvaluationTime = 1;

	private static boolean isRunning = false, refreshContentNeeded = true;

	public Simulation() {
		instance = this;
		content = new SimulationContent();
		interactionProcessor = new InteractionProcessor(content);
		timeStepController = new TimeStepController();
		simulationComponents.add(timeStepController);
		simulationComponents.add(interactionProcessor);
	}

	public static Simulation getInstance() {
		if (instance != null)
			return instance;
		else
			instance = new Simulation();
		return instance;
	}

	@Override
	public void run() {
		isRunning = true;
		ConsoleWindow.println(GUIStrings.SIMULATION_THREAD_STARTED);
		interactionProcessor.recalculateNeighborsNeeded();
		while (isRunning && time < stopTime) {
			long t0 = System.nanoTime();
			if (refreshContentNeeded)
				refreshContent();
			perfomStep();
			stepEvaluationTime = System.nanoTime() - t0;
		}
		stopTime = Double.MAX_VALUE;
		ConsoleWindow.println(GUIStrings.SIMULATION_THREAD_ENDED);
	}

	public boolean isActive() {
		return isRunning;
	}

	private void perfomStep() {
		time += timeStepController.getTimeStepSize();
		for (SimulationComponent component : simulationComponents)
			component.process();
	}

	public long perfomStep(int stepNumber, boolean consoleOutput) {
		long t1 = System.nanoTime();
		int n1 = interactionProcessor.getNeighborSearchsNumber();
		ConsoleWindow.println(GUIStrings.TIMESTEP + " " + timeStepController.getTimeStepSize() + " s");
		for (int i = 1; i < stepNumber; i++)
			perfomStep();
		long t2 = System.nanoTime();
		if (consoleOutput) {
			int n2 = interactionProcessor.getNeighborSearchsNumber();
			ConsoleWindow.println("Done " + stepNumber + " steps");
			ConsoleWindow.println("	elapsed: " + (t2 - t1) / 1E6 + " ms");
			ConsoleWindow.println("	neighbor searches: " + (n2 - n1));
			ConsoleWindow.println(GUIStrings.TIMESTEP + " " + timeStepController.getTimeStepSize() + " s");
		}
		return t2 - t1;
	}

	public void perfomSimulation(double duration) {
		instance.setSimulationDuration(duration);
		instance.run();
	}

	private void waitForStepComplete() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			ConsoleWindow.println(GUIStrings.SIMULATION_THREAD_CANT_BE_CONTINUED);
			e.printStackTrace();
		}
	}

	public void stopSimulation() {
		stopSimulationEchoOff();
	}

	private void stopSimulationEchoOff() {
		isRunning = false;
	}

	private void stopSimulationAndWait() {
		if (isRunning) {
			isRunning = false;
			waitForStepComplete();
		}
	}

	public void addToSimulation(Particle p) {
		if (isRunning) {
			pForAdd.add(p);
			refreshContentNeeded = true;
		} else {
			content.particles.add(p);
		}
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public void addToSimulation(Spring s) {
		if (isRunning) {
			sForAdd.add(s);
			refreshContentNeeded = true;
		} else {
			content.springs.add(s);
		}
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public void addToSimulation(ParticleGroup pp) {
		if (isRunning) {
			pForAdd.addAll(pp);
			refreshContentNeeded = true;
		} else {
			content.particles.addAll(pp);
		}
		interactionProcessor.recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.PARTICLES_ADDED);
	}

	public void addToSimulation(SpringGroup ss) {
		if (isRunning) {
			sForAdd.addAll(ss);
			refreshContentNeeded = true;
		} else {
			content.springs.addAll(ss);
		}
		interactionProcessor.recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.SPRINGS_ADDED);
	}

	public synchronized void addToSimulation(SimulationComponent arg) {
		boolean wasActive = false;
		if (isRunning) {
			stopSimulationAndWait();
			wasActive = true;
		}
		simulationComponents.add((SimulationComponent) arg);
		ConsoleWindow.println(GUIStrings.TO_SIMULATION_ADDED + " " + arg.getClass().getSimpleName());
		if (wasActive)
			MainWindow.getInstance().startSimulationThread();
	}

	public void removeParticleSafety(Particle p) {
		if (!content.springs.isEmpty())
			removeSpringsSafety(content.springs.findAttachedSprings(p));
		if (isRunning) {
			pForRemove.add(p);
			refreshContentNeeded = true;
		} else
			content.particles.remove(p);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	private synchronized void removeParticles(ParticleGroup pp) {
		if (!content.springs.isEmpty())
			for (Particle p : pp)
				removeSprings(content.springs.findAttachedSprings(p));
		content.particles.removeAll(pp);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public synchronized void removeParticlesSafety(ParticleGroup pp) {
		if (!content.springs.isEmpty())
			for (Particle p : pp)
				removeSpringsSafety(content.springs.findAttachedSprings(p));
		if (isRunning) {
			pForRemove.addAll(pp);
			refreshContentNeeded = true;
		} else
			content.particles.removeAll(pp);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public void removeRandomParticles(int number) {
		for (int i = 0; i < number; i++)
			removeParticleSafety(content.particle((int) Math.round(Math.random() * content.particles.size())));
	}

	private void removeSpring(Spring s) {
		content.deselect(s);
		content.springs.remove(s);
	}

	public void removeSpringSafety(Spring s) {
		if (isRunning) {
			sForRemove.add(s);
			refreshContentNeeded = true;
		} else
			content.springs.remove(s);
	}

	private synchronized void removeSprings(SpringGroup ss) {
		for (Spring s : ss)
			removeSpring(s);
	}

	public void removeSpringsSafety(SpringGroup ss) {
		if (isRunning) {
			sForRemove.addAll(ss);
			refreshContentNeeded = true;
		} else
			content.springs.removeAll(ss);
	}

	public void clearSimulation() {
		ConsoleWindow.clearConsole();
		ConsoleWindow.print(GUIStrings.FORCE_SIMULATION_STOP + " ");
		stopSimulationAndWait();
		ConsoleWindow.println(GUIStrings.DONE);
		reset();
	}

	public Spring getLastAddedSpring() {
		return content.springs.get(content.springs.size() - 1);
	}

	private void reset() {
		content.deselectAll();
		refreshContentNeeded = false;
		time = 0;
		stepEvaluationTime = 1;
		content.springs.clear();
		content.particles.clear();
		simulationComponents.clear();
		simulationComponents.add(timeStepController);
		simulationComponents.add(interactionProcessor);
		ConsoleWindow.println(GUIStrings.CLEARED);
		interactionProcessor.reset();
		timeStepController.resetTimeStep();
		new SampleScenes().emptyScene();
	}

	private void refreshContent() {
		removeParticles(pForRemove);
		content.particles.addAll(pForAdd);
		pForAdd.clear();
		removeSprings(sForRemove);
		content.springs.addAll(sForAdd);
		sForAdd.clear();
		refreshContentNeeded = false;
	}

	public double x(int i) {
		return content.particle(i).getX();
	}

	public double y(int i) {
		return content.particle(i).getY();
	}

	public double getTime() {
		return time;
	}

	public void setSimulationDuration(double duration) {
		instance.stopTime = time + duration;
	}

	public long getEvaluationTimeNanos() {
		return stepEvaluationTime;
	}

	public SimulationContent content() {
		return content;
	}

}