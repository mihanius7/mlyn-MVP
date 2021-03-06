package gui;

import static constants.PhysicalConstants.cm;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import elements.force_pair.Spring;
import elements.point_mass.Particle;
import file.SAXelementParser;
import gui.ViewportEvent.MouseMode;
import gui.editing.EditBoundariesWindow;
import gui.lang.GUIStrings;
import gui.lang.International;
import gui.lang.Language;
import gui.menu.MainWindowMenu;
import main.SampleScenes;
import simulation.Simulation;
import simulation.components.TimeStepController;
import simulation.components.TimeStepController.TimeStepMode;

public class MainWindow extends JFrame {

	public static final int RECORDER_MAX_ROWS = 8;
	private static MainWindow instance;
	private static final long serialVersionUID = 6398390928434245781L;
	public static Simulation simulation;
	public static Viewport viewport;
	private static MainWindowMenu menuBar;
	private static MainWindowEvent listener;
	private static ViewportEvent viewportListener;
	private static EditBoundariesWindow ebw;
	public static Thread simulationThread, renderingThread;
	public static int viewportInitWidth = 960, viewportInitHeight = 512;
	private static JTextArea outputTextArea;
	private static JScrollPane scrollArea;
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
		viewport = new Viewport(viewportInitWidth, viewportInitHeight);
		viewport.setBackground(UIManager.getColor("Button.light"));
		viewport.setBorder(null);
		listener = new MainWindowEvent(this);
		viewportListener = new ViewportEvent(viewport, this);
		getContentPane().setLayout(null);
		getContentPane().add(viewport);

		menuBar = new MainWindowMenu();
		setJMenuBar(menuBar);
		createButtons();
		createDialogs();

		changeLanguage(askForLanguage());

		setFocusTo(Simulation.getReferenceParticle());
		Simulation.getReferenceSpring();

		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setFocusable(true);
		setBounds(0, 0, 1024, 768);
		setLocationRelativeTo(null);
		setVisible(true);
		setCaption(GUIStrings.NEW_PROJECT_NAME);

		viewport.addKeyListener(viewportListener);
		viewport.addMouseListener(viewportListener);
		viewport.addMouseMotionListener(viewportListener);
		viewport.addMouseWheelListener(viewportListener);
		viewport.grabFocus();

		new SampleScenes().initializeScene();
		refreshGUIControls();

		startViewportRepaintThread();
		startSimulationThread();

		resizeGUI();
		viewport.refreshStaticSizeConstants();
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

		outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		outputTextArea.setFont(new Font("Monospaced", Font.BOLD, 12));
		outputTextArea.setFocusable(false);
		outputTextArea.setBackground(new Color(204, 204, 204));

		scrollArea = new JScrollPane();
		scrollArea.setViewportView(outputTextArea);
		getContentPane().add(scrollArea);

		labelTimeStep = new JLabel();
		labelTimeStep.setHorizontalAlignment(SwingConstants.TRAILING);

		getContentPane().add(labelTimeStep);

		buttonDecrease = new JButton("<<");
		buttonDecrease.addActionListener(listener);

		getContentPane().add(buttonDecrease);

		buttonIncrease = new JButton(">>");
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
		buttonTimeStepMode.setText(Simulation.timeStepController.getMode()==TimeStepMode.FIXED ? GUIStrings.TIMESTEP_FIXED : GUIStrings.TIMESTEP_DYNAMIC);
	}

	public void changeLanguage(Language lang) {
		International.prepareStrings(lang);
		applyLabels();
		menuBar.applyLabels();
		MainWindow.println("Language changed to " + lang);
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
		outputTextArea.setBounds(4, getHeight() - 130 - 67, getWidth() - 26, 128);
		scrollArea.setBounds(outputTextArea.getBounds());
		viewport.setBounds(0, 0, getWidth() - 14, getHeight() - outputTextArea.getHeight() - 76 - buttonStart.getHeight());
		viewport.refreshStaticSizeConstants();
		viewport.initTracksImage();
		int buttonsY = getHeight() - scrollArea.getHeight() - 97;
		buttonStart.setBounds(getWidth() - 215, buttonsY, 192, 24);
		labelTimeStep.setBounds(1, buttonsY + 4, 89, 16);
		buttonDecrease.setBounds(228, buttonsY, 48, 24);
		buttonIncrease.setBounds(288, buttonsY, 48, 24);
		buttonRealScale.setBounds(348, buttonsY, 56, 24);
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
			Simulation.interactionProcessor.recalculateNeighborsNeeded();
			simulationThread = new Thread(simulation);
			simulationThread.setPriority(3);
			simulationThread.start();
		}
	}

	public void stopSimulationThread() {
		Simulation.stopSimulation();
	}

	public void startViewportRepaintThread() {
		renderingThread = new Thread(viewport);
		renderingThread.setPriority(5);
		renderingThread.start();
	}

	public void setFocusTo(Particle p) {
		if (Simulation.getSelectedParticles().size() > 0)
			Simulation.getSelectedParticle(Simulation.getSelectedParticles().size() - 1);
		else {
		}
	}

	public void setFocusTo(Spring s) {
		if (Simulation.getSelectedSprings().size() > 0)
			Simulation.getSelectedSpring(Simulation.getSelectedSprings().size() - 1);
		System.out.println("Spring angle, rad: " + s.defineAngle());
	}

	public void clearSelection() {
		Simulation.clearSelection();
		setFocusTo(Simulation.getReferenceParticle());
	}

	public void applyReferenceParticleParameters() {
		Viewport.setMouseMode(MouseMode.PARTICLE_ADD);
		clearSelection();
		refreshGUIControls();
	}

	public void refreshGUIControls() {
		menuBar.refreshItems();
		buttonTimeStepMode.setText(Simulation.timeStepController.getMode()==TimeStepMode.FIXED ? GUIStrings.TIMESTEP_FIXED : GUIStrings.TIMESTEP_DYNAMIC);
		Double.toString(Viewport.getGridSize() / cm);
	}

	public void refreshGUIDisplays() {
		if (Simulation.getInstance().isActive())
			refreshTimeStepReserveDisplay();
		Viewport.getScale();
	}

	private void refreshTimeStepReserveDisplay() {
		double r = Simulation.interactionProcessor.getTimeStepReserveRatio();
		if (r > 10000)
			r = 10000;
	}

	public static void print(String s) {
		if (outputTextArea != null) {
			outputTextArea.append(s);
		}
	}

	public static void println(String s) {
		if (outputTextArea != null) {
			outputTextArea.append(outputTextArea.getLineCount() + ": " + s + "\n");
			outputTextArea.scrollRectToVisible(new java.awt.Rectangle(0, outputTextArea.getHeight(), 1, 1));
		}
	}

	public static void clearConsole() {
		if (outputTextArea != null) {
			outputTextArea.setText("");
		}
	}

	public static void showEditBoundariesWindow() {
		if (ebw != null)
			ebw.view();
		else
			ebw = new EditBoundariesWindow();
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
		Simulation.stopSimulation();
		int ret = openSceneChooser.showDialog(null, GUIStrings.OPEN_SCENE_DIALOG);
		if (ret == JFileChooser.APPROVE_OPTION) {
			Simulation.clearSimulation();
			File selectedFile = openSceneChooser.getSelectedFile();
			parser.loadFromFile(selectedFile);
			setCaption(selectedFile.getName());
			Simulation.perfomStep(5);
			refreshGUIControls();
			Viewport.scaleToAllParticles();
		} else if (ret == JFileChooser.CANCEL_OPTION) {
			startSimulationThread();
		}
	}

	public void setSelectedNextSpring(boolean previous) {
		if (Viewport.getMouseMode() == MouseMode.SPRING_SELECT)
			if (!previous) {
				Simulation.addToSelectionNextSpring();
				setFocusTo(Simulation.getSelectedSpring(0));
			} else {
				Simulation.addToSelectionPreviousSpring();
				setFocusTo(Simulation.getSelectedSpring(0));
			}
		else {
			Viewport.setMouseMode(MouseMode.SPRING_SELECT);
			setSelectedNextSpring(previous);
		}
	}

	public static void setCaption(String name) {
		instance.setTitle(name + " - " + GUIStrings.APP_NAME);
	}

	public void saveImageToFile() {
		viewport.saveImageToFile();
	}
}