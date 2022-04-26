package gui;

import static constants.PhysicalConstants.cm;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
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
import main.SampleScenes;
import simulation.Simulation;

public class MainWindow extends JFrame {

	public static final String OPEN_SCENE_DIALOG = "Адчыніць файл сцэны";
	public static final String IMAGE_SAVING_DIALOG = "Захаванне выявы";
	public static final String FILE_WRITING_ERROR_MESSAGE = "Памылка падчас захавання ў файл";
	public static final String FILE_ACESS_DENIED = "Файл заняты";
	public static final String CANT_WRITE_FILE_MESSAGE = "Немагчыма пісаць дадзеныя ў файл";
	public static final String NO_VIBRATOR_FOR_AFCH_MESSAGE = "Адсутнічае вібрастэнд, неабходны каб пісаць частотны водгук сістэмы";
	public static final String VIBRATION_DIALOG = "Вібрацыя";
	public static final String EMPTY_VIBRATOR_MESSAGE = "Няма аніводнай часціцы, звязанай са стэндам!";
	public static final String SELECT_PARTICLE_DIALOG = "Выбраць часціцу";
	public static final String NOTHING_SELECTED_MESSAGE = "Нічога не абрана";
	public static final String ABOUT = "Праграма для мадэлявання паводзінаў у часе простых механічных сістэм";
	public static final String FILETYPE_DESCRIPTION = "Сцэны для сімуляцыі";
	public static final String TIMESTEP_FIXED = "\u041F\u0430\u0441\u0442\u0430\u044F\u043D\u043D\u044B";
	public static final String TIMESTEP_LABEL = "\u041A\u0440\u043E\u043A \u043F\u0430 \u0447\u0430\u0441\u0435:";
	public static final String START_PAUSE_BUTTON = "\u0421\u0442\u0430\u0440\u0442/\u043F\u0430\u045E\u0437\u0430 \u0441\u0456\u043C\u0443\u043B\u044F\u0446\u044B\u0456";
	public static final String APP_NAME = "Млын2D (v.0.9)";
	private static MainWindow instance;
	private static final long serialVersionUID = 6398390928434245781L;
	public static Simulation simulation;
	public static Viewport viewport;
	private static MainWindowMenu menuBar;
	private static MainWindowEvent mwe;
	private static ViewportEvent ve;
	private static EditBoundariesWindow ebw;
	public static Thread calc, paint;
	public static int viewportInitWidth = 960, viewportInitHeight = 512;
	public static final int RECORDER_MAX_ROWS = 8;

	JButton startButton, dtDecrease, dtFix, dtIncrease, dtRealScale;
	private static JTextArea textArea1;
	private static JScrollPane scrollArea;
	private JLabel lblDt;
	private JFileChooser openSceneChooser, saveSceneChooser;

	public MainWindow() {
		instance = this;
		simulation = new Simulation();
		viewport = new Viewport(viewportInitWidth, viewportInitHeight);
		viewport.setBackground(UIManager.getColor("Button.light"));
		viewport.setBorder(null);
		mwe = new MainWindowEvent(this);
		ve = new ViewportEvent(viewport, this);
		getContentPane().setLayout(null);
		getContentPane().add(viewport);

		initMenu();
		initButtonsAndOthers();
		initDialogs();

		setFocusTo(Simulation.getReferenceParticle());
		Simulation.getReferenceSpring();

		setResizable(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage("Letter-m-icon.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setFocusable(true);
		setBounds(0, 0, 1024, 768);
		setLocationRelativeTo(null);
		setVisible(true);
		setCaption("Новы");

		viewport.addKeyListener(ve);
		viewport.addMouseListener(ve);
		viewport.addMouseMotionListener(ve);
		viewport.addMouseWheelListener(ve);
		viewport.grabFocus();

		new SampleScenes().initializeFirstScene();
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

	private void initMenu() {
		menuBar = new MainWindowMenu();
		setJMenuBar(menuBar);
	}

	private void initButtonsAndOthers() {

		addComponentListener(mwe);

		startButton = new JButton(START_PAUSE_BUTTON);

		startButton.setBackground(UIManager.getColor("Button.background"));

		startButton.setFocusable(false);
		startButton.setFocusCycleRoot(false);
		startButton.addActionListener(mwe);
		getContentPane().add(startButton);

		new ButtonGroup();
		new ButtonGroup();

		textArea1 = new JTextArea();
		textArea1.setLineWrap(true);
		textArea1.setFont(new Font("Monospaced", Font.BOLD, 12));
		textArea1.setFocusable(false);
		textArea1.setBackground(new Color(204, 204, 204));

		scrollArea = new JScrollPane();
		scrollArea.setViewportView(textArea1);
		getContentPane().add(scrollArea);

		lblDt = new JLabel(TIMESTEP_LABEL);
		lblDt.setHorizontalAlignment(SwingConstants.TRAILING);

		getContentPane().add(lblDt);

		dtDecrease = new JButton("<<");
		dtDecrease.addActionListener(mwe);

		getContentPane().add(dtDecrease);

		dtIncrease = new JButton(">>");
		dtIncrease.addActionListener(mwe);

		getContentPane().add(dtIncrease);

		dtRealScale = new JButton("1:1");
		dtRealScale.addActionListener(mwe);

		getContentPane().add(dtRealScale);

		dtFix = new JButton(TIMESTEP_FIXED);
		dtFix.setFont(new Font("Dialog", Font.BOLD, 11));
		dtFix.addActionListener(mwe);

		getContentPane().add(dtFix);

	}

	void resizeGUI() {
		textArea1.setBounds(4, getHeight() - 130 - 67, getWidth(), 128);
		scrollArea.setBounds(textArea1.getBounds());
		int buttonsY = getHeight() - scrollArea.getHeight() - 97;
		startButton.setBounds(getWidth() - 216, buttonsY, 192, 24);
		viewport.setBounds(0, 0, getWidth() - 14, getHeight() - textArea1.getHeight() - 76 - startButton.getHeight());
		viewport.refreshStaticSizeConstants();
		viewport.initTracksImage();
		lblDt.setBounds(1, buttonsY + 4, 89, 16);
		dtDecrease.setBounds(228, buttonsY, 48, 24);
		dtIncrease.setBounds(288, buttonsY, 48, 24);
		dtRealScale.setBounds(348, buttonsY, 56, 24);
		dtFix.setBounds(108, buttonsY, 108, 24);
	}

	private void initDialogs() {
		openSceneChooser = new JFileChooser(new java.io.File("").getAbsolutePath());
		saveSceneChooser = new JFileChooser(new java.io.File("").getAbsolutePath());
		FileFilter filter = new FileNameExtensionFilter(FILETYPE_DESCRIPTION, "xml");
		openSceneChooser.setFileFilter(filter);
		saveSceneChooser.setFileFilter(filter);
	}

	public void startSimulationThread() {
		if (calc == null || !calc.isAlive()) {
			Simulation.interactionProcessor.recalculateNeighborsNeeded();
			calc = new Thread(simulation);
			calc.setPriority(3);
			calc.start();
		}
	}

	public void stopSimulationThread() {
		Simulation.stopSimulation();
	}

	public void startViewportRepaintThread() {
		paint = new Thread(viewport);
		paint.setPriority(5);
		paint.start();
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
		dtFix.setText(Simulation.timeStepController.getMode().toString());
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
		if (textArea1 != null) {
			textArea1.append(s);
		}
	}

	public static void println(String s) {
		if (textArea1 != null) {
			textArea1.append(textArea1.getLineCount() + ": " + s + "\n");
			textArea1.scrollRectToVisible(new java.awt.Rectangle(0, textArea1.getHeight(), 1, 1));
		}
	}

	public static void clearConsole() {
		if (textArea1 != null) {
			textArea1.setText("");
		}
	}

	public static void showEditBoundariesWindow() {
		if (ebw != null)
			ebw.view();
		else
			ebw = new EditBoundariesWindow();
	}

	public void showAboutWindow() {
		JOptionPane.showMessageDialog(null, ABOUT,
				"Аб праграме " + APP_NAME, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void NothingIsSelectedMessage() {
		JOptionPane.showMessageDialog(null, NOTHING_SELECTED_MESSAGE, SELECT_PARTICLE_DIALOG, JOptionPane.WARNING_MESSAGE);
	}

	public static void VibratorIsEmptyMessage() {
		JOptionPane.showMessageDialog(null, EMPTY_VIBRATOR_MESSAGE, VIBRATION_DIALOG,
				JOptionPane.WARNING_MESSAGE);
	}

	public static void uncompatibleRecordingModeMessage() {
		JOptionPane.showMessageDialog(null, NO_VIBRATOR_FOR_AFCH_MESSAGE,
				"Рэгістратар", JOptionPane.WARNING_MESSAGE);
	}

	public static void fileWriteErrorMessage(String fileName) {
		JOptionPane.showMessageDialog(null, CANT_WRITE_FILE_MESSAGE + " " + fileName, FILE_ACESS_DENIED,
				JOptionPane.ERROR_MESSAGE);
	}

	public static void imageWriteErrorMessage(String fileName) {
		JOptionPane.showMessageDialog(null, FILE_WRITING_ERROR_MESSAGE + " " + fileName, IMAGE_SAVING_DIALOG,
				JOptionPane.ERROR_MESSAGE);
	}

	public void openSceneDialog() {
		SAXelementParser parser = new SAXelementParser();
		Simulation.stopSimulation();
		int ret = openSceneChooser.showDialog(null, OPEN_SCENE_DIALOG);
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
		instance.setTitle(name + " - " + APP_NAME);
	}

	public void saveImageToFile() {
		viewport.saveImageToFile();
	}
}