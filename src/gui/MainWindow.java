package gui;

import static calculation.constants.PhysicalConstants.cm;

import java.awt.Font;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import elements.line.Spring;
import elements.point.Particle;
import gui.dialogs.EditBoundariesWindow;
import gui.lang.GUIStrings;
import gui.lang.International;
import gui.lang.Language;
import gui.menu.MainWindowMenu;
import gui.viewport.Viewport;
import gui.viewport.listeners.MouseMode;
import io.SAXelementParser;
import simulation.Simulation;
import simulation.components.TimeStepController.TimeStepMode;
import test.SampleScenes;

public class MainWindow extends JFrame {

	public final int RECORDER_MAX_ROWS = 8;
	private static MainWindow instance;
	public Simulation simulation;
	public Viewport viewport;
	public ConsoleWindow consoleWindow;
	private MainWindowMenu menuBar;
	private MainWindowEvent listener;
	private EditBoundariesWindow ebw;
	public Thread simulationThread, renderingThread;
	public int viewportInitWidth = 960, viewportInitHeight = 512;
	private JLabel labelTimeStep;
	private JFileChooser openSceneChooser, saveSceneChooser;
	JButton buttonStart;
	JButton buttonDecrease;
	JButton buttonTimeStepMode;
	JButton buttonIncrease;
	JButton buttonRealScale;

	public MainWindow() {
		instance = this;
		simulation = new Simulation();
		viewport = new Viewport(viewportInitWidth, viewportInitHeight, this);
		viewport.setBackground(UIManager.getColor("Button.light"));
		viewport.setBorder(null);
		listener = new MainWindowEvent(this);
		getContentPane().setLayout(null);
		getContentPane().add(viewport);

		menuBar = new MainWindowMenu(viewport);
		setJMenuBar(menuBar);
		createButtons();
		createDialogs();

		changeLanguage(Language.ENGLISH);
		
		consoleWindow = new ConsoleWindow();

		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setFocusable(true);
		setBounds(0, 0, 960, 540);
		setLocationRelativeTo(null);
		setVisible(true);
		setCaption(GUIStrings.NEW_PROJECT_NAME);

		viewport.grabFocus();

		new SampleScenes().initializeScene();
		refreshGUIControls();

		startViewportRepaintThread();
		startSimulationThread();
		
		viewport.scaleToBoundaries();

		resizeGUI();
	}

	public static MainWindow getInstance() {
		if (instance != null)
			return instance;
		else
			instance = new MainWindow();
		return instance;
	}

	private void createButtons() {

		addComponentListener(listener);

		buttonStart = new JButton();
		buttonStart.setFocusable(false);
		buttonStart.setFocusCycleRoot(false);
		buttonStart.addActionListener(listener);
		getContentPane().add(buttonStart);

		labelTimeStep = new JLabel();
		labelTimeStep.setHorizontalAlignment(SwingConstants.TRAILING);

		getContentPane().add(labelTimeStep);

		buttonDecrease = new JButton("-");
		buttonDecrease.addActionListener(listener);

		getContentPane().add(buttonDecrease);

		buttonIncrease = new JButton("+");
		buttonIncrease.addActionListener(listener);

		getContentPane().add(buttonIncrease);

		buttonRealScale = new JButton("1:1");
		buttonRealScale.addActionListener(listener);

		getContentPane().add(buttonRealScale);

		buttonTimeStepMode = new JButton();
		buttonTimeStepMode.setFont(new Font("Dialog", Font.BOLD, 11));
		buttonTimeStepMode.addActionListener(listener);

		getContentPane().add(buttonTimeStepMode);

	}

	private void applyLabels() {
		labelTimeStep.setText(GUIStrings.TIMESTEP_LABEL);
		buttonStart.setText(GUIStrings.START_PAUSE_BUTTON);
		buttonTimeStepMode.setText(Simulation.getInstance().timeStepController.getMode()==TimeStepMode.FIXED ? GUIStrings.TIMESTEP_FIXED : GUIStrings.TIMESTEP_DYNAMIC);
	}

	public void changeLanguage(Language lang) {
		International.prepareStrings(lang);
		applyLabels();
		menuBar.applyLabels();
		ConsoleWindow.println("Language changed to " + lang);
	}

	public Language askForLanguage() {
		String[] possibleStrings = Arrays.toString(Language.values()).replaceAll("^.|.$", "").split(", ");
		String selectedString = (String) JOptionPane.showInputDialog(null,
				"Select language / \u0410\u0431\u044F\u0440\u044B\u0446\u0435 \u043C\u043E\u0432\u0443", "Welcome!",
				JOptionPane.INFORMATION_MESSAGE, null, possibleStrings, possibleStrings[0]);
		if (selectedString != null)
			return Language.valueOf(selectedString);
		else
			return Language.ENGLISH;
	}

	void resizeGUI() {
		viewport.setBounds(0, 0, getWidth() - 14, getHeight() - 76 - buttonStart.getHeight());
		viewport.resizeFrameImage();
		viewport.initBackgroundImage();
		viewport.initTracksImage();
		viewport.initHeatMapImage();
		int buttonsY = getHeight() - 92;
		buttonStart.setBounds(getWidth() - 215, buttonsY, 192, 24);
		labelTimeStep.setBounds(1, buttonsY + 4, 89, 16);
		buttonDecrease.setBounds(223, buttonsY, 56, 24);
		buttonIncrease.setBounds(286, buttonsY, 56, 24);
		buttonRealScale.setBounds(348, buttonsY, 64, 24);
		buttonTimeStepMode.setBounds(108, buttonsY, 108, 24);
	}

	private void createDialogs() {
		openSceneChooser = new JFileChooser(new java.io.File("").getAbsolutePath());
		saveSceneChooser = new JFileChooser(new java.io.File("").getAbsolutePath());
		FileFilter filter = new FileNameExtensionFilter(GUIStrings.FILETYPE_DESCRIPTION, "xml");
		openSceneChooser.setFileFilter(filter);
		saveSceneChooser.setFileFilter(filter);
	}

	public void startSimulationThread() {
		if (simulationThread == null || !simulationThread.isAlive()) {
			Simulation.getInstance().interactionProcessor.recalculateNeighborsNeeded();
			simulationThread = new Thread(simulation);
			simulationThread.setPriority(3);
			simulationThread.start();
		}
	}

	public void stopSimulationThread() {
		Simulation.getInstance().stop();
	}

	public void startViewportRepaintThread() {
		renderingThread = new Thread(viewport);
		renderingThread.setPriority(5);
		renderingThread.start();
	}

	public void setFocusTo(Particle p) {
		if (Simulation.getInstance().content().getSelectedParticles().size() > 0)
			Simulation.getInstance().content().getSelectedParticle(Simulation.getInstance().content().getSelectedParticles().size() - 1);
	}

	public void setFocusTo(Spring s) {
		if (Simulation.getInstance().content().getSelectedSprings().size() > 0)
			Simulation.getInstance().content().getSelectedSpring(Simulation.getInstance().content().getSelectedSprings().size() - 1);
	}

	public void clearSelection() {
		Simulation.getInstance().content().deselectAll();
		setFocusTo(Simulation.getInstance().content().getReferenceParticle());
	}

	public void applyReferenceParticleParameters() {
		viewport.setMouseMode(MouseMode.ADD_PARTICLE);
		clearSelection();
		refreshGUIControls();
	}

	public void refreshGUIControls() {
		menuBar.refreshItems();
		buttonTimeStepMode.setText(Simulation.getInstance().timeStepController.getMode()==TimeStepMode.FIXED ? GUIStrings.TIMESTEP_FIXED : GUIStrings.TIMESTEP_DYNAMIC);
		Double.toString(viewport.getGridSize() / cm);
	}

	public void refreshGUIDisplays() {
		if (Simulation.getInstance().isActive())
			refreshTimeStepReserveDisplay();
	}

	private void refreshTimeStepReserveDisplay() {
		double r = Simulation.getInstance().interactionProcessor.getTimeStepReserveRatio();
		if (r > 10000)
			r = 10000;
	}

	public void showEditBoundariesWindow() {
		if (ebw != null)
			ebw.view();
		else
			ebw = new EditBoundariesWindow(viewport);
	}
	
	public void showConsoleWindow() {
		consoleWindow.setVisible(true);
	}

	public void showAboutWindow() {
		JOptionPane.showMessageDialog(null, GUIStrings.ABOUT, "About " + GUIStrings.APP_NAME,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void NothingIsSelectedMessage() {
		JOptionPane.showMessageDialog(null, GUIStrings.NOTHING_SELECTED_MESSAGE, GUIStrings.SELECT_PARTICLE_DIALOG,
				JOptionPane.WARNING_MESSAGE);
	}

	public static void VibratorIsEmptyMessage() {
		JOptionPane.showMessageDialog(null, GUIStrings.EMPTY_VIBRATOR_MESSAGE, GUIStrings.VIBRATION_DIALOG,
				JOptionPane.WARNING_MESSAGE);
	}

	public static void uncompatibleRecordingModeMessage() {
		JOptionPane.showMessageDialog(null, GUIStrings.NO_VIBRATOR_FOR_AFCH_MESSAGE, GUIStrings.RECORDER_DIALOG,
				JOptionPane.WARNING_MESSAGE);
	}

	public static void fileWriteErrorMessage(String fileName) {
		JOptionPane.showMessageDialog(null, GUIStrings.CANT_WRITE_FILE_MESSAGE + " " + fileName,
				GUIStrings.FILE_ACESS_DENIED, JOptionPane.ERROR_MESSAGE);
	}

	public static void imageWriteErrorMessage(String fileName) {
		JOptionPane.showMessageDialog(null, GUIStrings.FILE_WRITING_ERROR_MESSAGE + " " + fileName,
				GUIStrings.IMAGE_SAVING_DIALOG, JOptionPane.ERROR_MESSAGE);
	}

	public void openSceneDialog() {
		SAXelementParser parser = new SAXelementParser();
		Simulation.getInstance().stop();
		int ret = openSceneChooser.showDialog(null, GUIStrings.OPEN_SCENE_DIALOG);
		if (ret == JFileChooser.APPROVE_OPTION) {
			Simulation.getInstance().clearAll();
			File selectedFile = openSceneChooser.getSelectedFile();
			parser.loadFromFile(selectedFile);
			setCaption(selectedFile.getName());
			Simulation.getInstance().perfomStep(5, true);
			refreshGUIControls();
			viewport.reset();
			viewport.scaleToAllParticles();
		} else if (ret == JFileChooser.CANCEL_OPTION) {
			startSimulationThread();
		}
	}

	public void setSelectedNextSpring(boolean previous) {
		if (viewport.getMouseMode() == MouseMode.SELECT_SPRING)
			if (!previous) {
				Simulation.getInstance().content().selectNextSpring();
				setFocusTo(Simulation.getInstance().content().getSelectedSpring(0));
			} else {
				Simulation.getInstance().content().selectPreviousSpring();
				setFocusTo(Simulation.getInstance().content().getSelectedSpring(0));
			}
		else {
			viewport.setMouseMode(MouseMode.SELECT_SPRING);
			setSelectedNextSpring(previous);
		}
	}

	public static void setCaption(String name) {
		instance.setTitle(name + " - " + GUIStrings.APP_NAME);
	}

	public void saveImageToFile() {
		viewport.saveScreenshot();
	}
}