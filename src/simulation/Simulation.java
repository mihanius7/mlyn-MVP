package simulation;

import static constants.PhysicalConstants.cm;
import static constants.PhysicalConstants.kg;

import java.util.ArrayList;
import java.util.Iterator;

import elements.force_pair.Spring;
import elements.groups.ParticleGroup;
import elements.groups.SpringGroup;
import elements.point_mass.Particle;
import evaluation.MyMath;
import evaluation.interaction.InteractionProcessor;
import gui.ConsoleWindow;
import gui.MainWindow;
import gui.Viewport;
import gui.lang.GUIStrings;
import gui.shapes.SpringShape;
import main.SampleScenes;
import simulation.components.OneTimePerStepProcessable;
import simulation.components.TimeStepController;

public class Simulation implements Runnable {

	private static Simulation instance;

	private static SimulationContent content = new SimulationContent();

	private static final ParticleGroup selectedParticles = new ParticleGroup();
	private static final ParticleGroup pForRemove = new ParticleGroup();
	private static final ParticleGroup pForAdd = new ParticleGroup();

	private static final SpringGroup selectedSprings = new SpringGroup();
	private static final SpringGroup sForRemove = new SpringGroup();
	private static final SpringGroup sForAdd = new SpringGroup();

	private static final ArrayList<OneTimePerStepProcessable> oneTimePerStepProcessables = new ArrayList<OneTimePerStepProcessable>();

	private static Particle referenceParticle;
	private static Spring referenceSpring;

	public static InteractionProcessor interactionProcessor;
	public static final TimeStepController timeStepController = new TimeStepController();

	private static double time = 0;
	private static int maxSelectedNumber = 1;
	private static long stepEvaluationTime = 1;

	private static boolean isRunning = false, refreshContentNeeded = true;

	public Simulation() {

		instance = this;
		interactionProcessor = new InteractionProcessor(content);
		oneTimePerStepProcessables.add(timeStepController);
		oneTimePerStepProcessables.add(interactionProcessor);

		referenceParticle = new Particle(0, 0, 1 * kg, 6 * cm);
		referenceParticle.setVisible(false);

		new MyMath();
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
		Simulation.interactionProcessor.recalculateNeighborsNeeded();
		while (isRunning) {
			long t0 = System.nanoTime();
			if (refreshContentNeeded)
				refreshContent();
			perfomStep();
			stepEvaluationTime = System.nanoTime() - t0;
		}
		ConsoleWindow.println(GUIStrings.SIMULATION_THREAD_ENDED);
	}

	public boolean isActive() {
		return isRunning;
	}

	private static void perfomStep() {
		time += timeStepController.getTimeStepSize();
		for (OneTimePerStepProcessable esp : oneTimePerStepProcessables)
			esp.process();
	}

	public static void perfomStep(int stepNumber) {
		long t = System.nanoTime();
		ConsoleWindow.println(GUIStrings.TIMESTEP + " " + timeStepController.getTimeStepSize() + " c");
		for (int i = 1; i < stepNumber; i++)
			perfomStep();
		ConsoleWindow.println("Done " + stepNumber + " steps");
		ConsoleWindow.println("	elapsed: " + (System.nanoTime() - t) / 1E6 + " ms");
		ConsoleWindow.println("	neighbor searches: " + interactionProcessor.getNeighborSearchsNumber());
		ConsoleWindow.println(GUIStrings.TIMESTEP + " " + timeStepController.getTimeStepSize() + " s");
	}

	public static void perfomSimulation(double simulationTime) {

	}

	private static void waitForStepComplete() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			ConsoleWindow.println(GUIStrings.SIMULATION_THREAD_CANT_BE_CONTINUED);
			e.printStackTrace();
		}
	}

	public static void stopSimulation() {
		stopSimulationEchoOff();
	}

	private static void stopSimulationEchoOff() {
		isRunning = false;
	}

	private static void stopSimulationAndWait() {
		if (isRunning) {
			isRunning = false;
			waitForStepComplete();
		}
	}

	public static void addToSimulation(Particle p) {
		if (isRunning) {
			pForAdd.add(p);
			refreshContentNeeded = true;
		} else
			content.particles.add(p);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public static void addToSimulation(Spring s) {
		if (isRunning) {
			sForAdd.add(s);
			refreshContentNeeded = true;
		} else
			content.springs.add(s);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public static void addToSimulation(ParticleGroup pp) {
		if (isRunning) {
			pForAdd.addAll(pp);
			refreshContentNeeded = true;
		} else
			content.particles.addAll(pp);
		interactionProcessor.recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.PARTICLES_ADDED);
	}

	public static void addToSimulation(SpringGroup ss) {
		if (isRunning) {
			sForAdd.addAll(ss);
			refreshContentNeeded = true;
		} else
			content.springs.addAll(ss);
		interactionProcessor.recalculateNeighborsNeeded();
		ConsoleWindow.println(GUIStrings.SPRINGS_ADDED);
	}

	public static synchronized void addToSimulation(OneTimePerStepProcessable arg) {
		boolean wasActive = false;
		if (isRunning) {
			stopSimulationAndWait();
			wasActive = true;
		}
		oneTimePerStepProcessables.add((OneTimePerStepProcessable) arg);
		ConsoleWindow.println(GUIStrings.TO_SIMULATION_ADDED +" " + arg.getClass().getSimpleName());
		if (wasActive)
			MainWindow.getInstance().startSimulationThread();
	}

	public static void removeParticleSafety(Particle p) {
		if (!content.springs.isEmpty())
			removeSpringsSafety(findAttachedSprings(p));
		if (isRunning) {
			pForRemove.add(p);
			refreshContentNeeded = true;
		} else
			content.particles.remove(p);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	private static void removeParticles(ParticleGroup pp) {
		if (!content.springs.isEmpty())
			for (Particle p : pp)
				removeSprings(findAttachedSprings(p));
		content.particles.removeAll(pp);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public static void removeParticlesSafety(ParticleGroup pp) {
		if (!content.springs.isEmpty())
			for (Particle p : pp)
				removeSpringsSafety(findAttachedSprings(p));
		if (isRunning) {
			pForRemove.addAll(pp);
			refreshContentNeeded = true;
		} else
			content.particles.removeAll(pp);
		interactionProcessor.recalculateNeighborsNeeded();
	}

	public static void removeRandomParticles(int number) {
		for (int i = 0; i < number; i++)
			removeParticleSafety(getParticle((int) Math.round(Math.random() * content.particles.size())));
	}

	private static void removeSpring(Spring s) {
		content.springs.remove(s);
	}

	public static void removeSpringSafety(Spring s) {
		if (isRunning) {
			sForRemove.add(s);
			refreshContentNeeded = true;
		} else
			content.springs.remove(s);
	}

	private static void removeSprings(SpringGroup ss) {
		for (Spring s : ss)
			removeSpring(s);
	}

	public static void removeSpringsSafety(SpringGroup ss) {
		if (isRunning) {
			sForRemove.addAll(ss);
			refreshContentNeeded = true;
		} else
			content.springs.removeAll(ss);
	}

	public static void clearSimulation() {
		ConsoleWindow.clearConsole();
		ConsoleWindow.print(GUIStrings.FORCE_SIMULATION_STOP + " ");
		stopSimulationAndWait();
		ConsoleWindow.println(GUIStrings.DONE);
		reset();
	}

	public static Particle getParticle(int i) {
		if (i < content.particles.size() && content.particles.get(i) != null)
			return content.particles.get(i);
		else {
			return null;
		}
	}

	public static Particle getLastAddedParticle() {
		return content.particles.get(content.particles.size() - 1);
	}

	public static Particle getReferenceParticle() {
		return referenceParticle;
	}

	public static Particle getParticleWithLesserIndex(Particle p1, Particle p2) {
		if (getIndex(p1) > getIndex(p2))
			return p2;
		else
			return p1;
	}

	public static Particle getParticleWithLargerIndex(Particle p1, Particle p2) {
		if (getIndex(p1) > getIndex(p2))
			return p1;
		else
			return p2;
	}

	public static Spring getReferenceSpring() {
		return referenceSpring;
	}

	public static int getIndex(Particle p) {
		return content.particles.indexOf((Particle) p);
	}

	public static int getIndex(Spring s) {
		return content.springs.indexOf((Spring) s);
	}

	public static Spring getSpring(int i) {
		if (i < content.springs.size() && content.springs.get(i) != null)
			return content.springs.get(i);
		else
			return null;
	}

	public static Spring getLastAddedSpring() {
		return content.springs.get(content.springs.size() - 1);
	}

	public static SpringGroup findAttachedSprings(Particle p) {
		SpringGroup returnList = new SpringGroup();
		for (int i = 0; i < content.springs.size(); i++) {
			Spring s = content.springs.get(i);
			if (s != null) {
				if (s.isHasParticle(p))
					returnList.add(s);
			}
		}
		return returnList;
	}

	public static Particle findNearestParticle(double x, double y, double maxDistance) {
		double minSqDist = Double.MAX_VALUE, sqDist;
		Particle nearest = null;
		for (Particle p : content.particles) {
			sqDist = MyMath.defineSquaredDistance(p, x, y) - MyMath.sqr(p.getRadius());
			if (sqDist < minSqDist) {
				minSqDist = sqDist;
				nearest = p;
			}
		}
		if (minSqDist > MyMath.sqr(maxDistance))
			nearest = null;
		return nearest;
	}

	public static Spring findNearestSpring(double x, double y, final double maxDistance) {
		double dist, margin;
		Spring nearest = null;
		for (Spring s : content.springs) {
			if (s.isLine())
				margin = maxDistance;
			else
				margin = SpringShape.SPRING_ZIGZAG_AMPLITUDE + s.getVisibleWidth() / 2;
			dist = MyMath.defineDistanceToLineSegment(s, x, y);
			if (dist < Math.max(s.getVisibleWidth() / 2, margin)) {
				nearest = s;
			}
		}
		return nearest;
	}

	private static void reset() {
		clearSelection();
		refreshContentNeeded = false;
		time = 0;
		stepEvaluationTime = 1;
		content.springs.clear();
		content.particles.clear();
		oneTimePerStepProcessables.clear();
		oneTimePerStepProcessables.add(timeStepController);
		oneTimePerStepProcessables.add(interactionProcessor);
		ConsoleWindow.println(GUIStrings.CLEARED);
		interactionProcessor.reset();
		timeStepController.resetTimeStep();
		SampleScenes sampleScenes = new SampleScenes();
		sampleScenes.emptyScene();
	}

	private static void refreshContent() {
		removeParticles(pForRemove);
		content.particles.addAll(pForAdd);
		pForAdd.clear();
		removeSprings(sForRemove);
		content.springs.addAll(sForAdd);
		sForAdd.clear();
		refreshContentNeeded = false;
	}

	public static int getParticlesCount() {
		return content.particles.size();
	}

	public static int getSpringCount() {
		return content.springs.size();
	}

	public static double x(int i) {
		return getParticle(i).getX();
	}

	public static double y(int i) {
		return getParticle(i).getY();
	}

	public static double getTime() {
		return time;
	}

	public static void addToSelection(Particle p) {
		if (selectedParticles.size() < maxSelectedNumber) {
			selectedParticles.add(p);
			p.select();
		}
	}

	public static void addToSelection(Spring s) {
		if (selectedSprings.size() < maxSelectedNumber) {
			selectedSprings.add(s);
			s.select();
		}
	}

	public static void addToSelectionNextSpring() {
		if (selectedSprings.isEmpty())
			addToSelection(getSpring(0));
		else {
			selectedSprings.size();
			int lastSelectedIndex = getIndex(selectedSprings.get(0));
			int nextSelectedIndex = lastSelectedIndex + 1;
			if (nextSelectedIndex > getSpringCount() - 1)
				nextSelectedIndex = 0;
			clearSelection();
			addToSelection(getSpring(nextSelectedIndex));
		}
	}

	public static void addToSelectionPreviousSpring() {
		if (selectedSprings.isEmpty())
			addToSelection(getSpring(0));
		else {
			selectedSprings.size();
			int lastSelectedIndex = getIndex(selectedSprings.get(0));
			int nextSelectedIndex = lastSelectedIndex - 1;
			if (nextSelectedIndex < 0)
				nextSelectedIndex = getSpringCount() - 1;
			clearSelection();
			addToSelection(getSpring(nextSelectedIndex));
		}
	}

	public static void addToSelectionAllParticles() {
		clearSelection();
		Particle p;
		Iterator<Particle> it = content.particles.iterator();
		while (it.hasNext()) {
			p = it.next();
			addToSelection(p);
		}
	}

	public static void removeFromSelection(Particle p) {
		selectedParticles.remove(p);
		p.deselect();
	}

	public static void removeFromSelection(Spring s) {
		selectedSprings.remove(s);
		s.deselect();
	}

	public static void clearSelection() {
		if (!selectedParticles.isEmpty() || !selectedSprings.isEmpty()) {
			for (Particle p : selectedParticles)
				p.deselect();
			for (Spring s : selectedSprings)
				s.deselect();
			selectedParticles.clear();
			selectedSprings.clear();
		}
	}

	public static Particle getSelectedParticle(int index) {
		if (index < selectedParticles.size())
			return selectedParticles.get(index);
		else
			return null;
	}

	public static Spring getSelectedSpring(int index) {
		if (index < selectedSprings.size())
			return selectedSprings.get(index);
		else
			return null;
	}

	public static void setMaxSelectionNumber(int i) {
		maxSelectedNumber = i;
	}

	public static void removeSelectedParticles() {
		if (!selectedParticles.isEmpty()) {
			removeParticlesSafety(selectedParticles);
			selectedParticles.clear();
		}
	}

	public static void removeSelectedSprings() {
		if (!selectedSprings.isEmpty()) {
			removeSpringsSafety(selectedSprings);
			selectedSprings.clear();
		}
	}

	public static ParticleGroup getParticles() {
		return content.particles;
	}

	public static ParticleGroup getSelectedParticles() {
		return selectedParticles;
	}

	public static SpringGroup getSprings() {
		return content.springs;
	}

	public static SpringGroup getSelectedSprings() {
		return selectedSprings;
	}

	public static long getEvaluationTimeNanos() {
		return stepEvaluationTime;
	}

	public static SimulationContent getContent() {
		return content;
	}

}