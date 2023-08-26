package gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import gui.MainWindow;
import gui.MouseMode;
import gui.Viewport;
import gui.lang.GUIStrings;
import gui.shapes.ParticleShape;
import simulation.Simulation;

public class MainWindowMenu extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroupMouse;
	private MainWindowMenuEvent menuListener;
	private JMenu menuFile;
	private JMenu menuEdit;
	private JMenu menuAdd;
	private JMenu menuSimulation;
	private JMenu menuShow;
	private JMenu menuControl;
	private JMenu menuHelp;
	private JMenu menuConsider;
	private JMenu menuMouse;
	JRadioButtonMenuItem itemMouseSelect2;
	JRadioButtonMenuItem itemMouseSelect1;
	JRadioButtonMenuItem itemAdd1;
	JRadioButtonMenuItem itemByPlace;
	JRadioButtonMenuItem itemByForce;
	JRadioButtonMenuItem itemAdd2;
	JMenuItem itemOpen;
	JMenuItem itemSave;
	JMenuItem itemExit;
	JMenuItem itemStart;
	JMenuItem itemSteps;
	JMenuItem itemClear;
	JMenuItem itemFreeze;
	JMenuItem itemBoundaries;
	JMenuItem itemConsole;
	JMenuItem itemAutoscale1;
	JMenuItem itemAutoscale2;
	JMenuItem itemSelectAll;
	JMenuItem itemAbout;
	JMenuItem itemScreenshot;
	JMenuItem itemLanguage;
	JMenuItem itemFix;
	JMenuItem itemConnect;
	JMenuItem itemFollow;
	JMenuItem itemCoM;
	JMenuItem itemDelete;
	JMenuItem itemSnap;
	JMenuItem itemColorizeByCharge;
	JCheckBoxMenuItem itemCollisions1;
	JCheckBoxMenuItem itemOuterForces;
	JCheckBoxMenuItem itemFriction;
	JCheckBoxMenuItem itemVelocities;
	JCheckBoxMenuItem itemForces;
	JCheckBoxMenuItem itemPretty;
	JCheckBoxMenuItem itemTags;
	JCheckBoxMenuItem itemGrid;
	JCheckBoxMenuItem itemTracks;
	private Viewport viewport;

	public MainWindowMenu(Viewport v) {
		
		viewport = v;

		buttonGroupMouse = new ButtonGroup();
		menuListener = new MainWindowMenuEvent(this, v);

		menuFile = new JMenu();
		menuFile.setMnemonic(KeyEvent.VK_F);
		itemOpen = new JMenuItem();
		itemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		itemOpen.addActionListener(menuListener);
		menuFile.add(itemOpen);
		itemSave = new JMenuItem();
		itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		itemSave.addActionListener(menuListener);
		itemSave.setEnabled(false);
		menuFile.add(itemSave);
		menuFile.addSeparator();
		itemClear = new JMenuItem();
		itemClear.addActionListener(menuListener);
		itemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ActionEvent.CTRL_MASK));
		menuFile.add(itemClear);
		menuFile.addSeparator();
		itemExit = new JMenuItem();
		menuFile.add(itemExit);
		itemExit.addActionListener(menuListener);
		add(menuFile);

		menuEdit = new JMenu();
		menuEdit.setMnemonic(KeyEvent.VK_E);
		itemMouseSelect2 = new JRadioButtonMenuItem();
		itemMouseSelect2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
		itemMouseSelect2.setSelected(false);
		itemMouseSelect2.addActionListener(menuListener);
		menuEdit.add(itemMouseSelect2);
		buttonGroupMouse.add(itemMouseSelect2);
		itemMouseSelect1 = new JRadioButtonMenuItem();
		itemMouseSelect1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK));
		itemMouseSelect1.setSelected(false);
		itemMouseSelect1.addActionListener(menuListener);
		menuEdit.add(itemMouseSelect1);
		buttonGroupMouse.add(itemMouseSelect1);
		itemSelectAll = new JMenuItem();
		itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		itemSelectAll.addActionListener(menuListener);
		menuEdit.add(itemSelectAll);
		menuEdit.addSeparator();
		itemDelete = new JMenuItem();
		itemDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		itemDelete.addActionListener(menuListener);
		menuEdit.add(itemDelete);
		itemFix = new JMenuItem();
		itemFix.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		itemFix.addActionListener(menuListener);
		menuEdit.add(itemFix);
		itemCoM = new JMenuItem();
		itemCoM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
		itemCoM.addActionListener(menuListener);
		menuEdit.add(itemCoM);
		itemSnap = new JMenuItem();
		itemSnap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		itemSnap.addActionListener(menuListener);
		menuEdit.add(itemSnap);
		add(menuEdit);

		menuAdd = new JMenu();
		menuAdd.setMnemonic(KeyEvent.VK_A);
		itemAdd1 = new JRadioButtonMenuItem();
		itemAdd1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		itemAdd1.setSelected(false);
		itemAdd1.addActionListener(menuListener);
		menuAdd.add(itemAdd1);
		buttonGroupMouse.add(itemAdd1);
		itemAdd2 = new JRadioButtonMenuItem();
		itemAdd2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		itemAdd2.setSelected(false);
		itemAdd2.addActionListener(menuListener);
		menuAdd.add(itemAdd2);
		buttonGroupMouse.add(itemAdd2);
		add(menuAdd);

		menuSimulation = new JMenu();
		menuSimulation.setMnemonic(KeyEvent.VK_S);
		itemStart = new JMenuItem();
		itemStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK));
		itemStart.addActionListener(menuListener);
		menuSimulation.add(itemStart);
		itemSteps = new JMenuItem();
		itemSteps.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
		itemSteps.addActionListener(menuListener);
		menuSimulation.add(itemSteps);
		menuSimulation.addSeparator();
		menuConsider = new JMenu();
		menuSimulation.add(menuConsider);
		itemCollisions1 = new JCheckBoxMenuItem();
		itemCollisions1.setMnemonic(KeyEvent.VK_C);
		itemCollisions1.addActionListener(menuListener);
		menuConsider.add(itemCollisions1);
		itemOuterForces = new JCheckBoxMenuItem();
		itemOuterForces.setMnemonic(KeyEvent.VK_G);
		itemOuterForces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		itemOuterForces.addActionListener(menuListener);
		menuConsider.add(itemOuterForces);
		itemFriction = new JCheckBoxMenuItem();
		itemFriction.setMnemonic(KeyEvent.VK_F);
		itemFriction.addActionListener(menuListener);
		menuConsider.add(itemFriction);
		menuSimulation.addSeparator();
		itemFreeze = new JMenuItem();
		itemFreeze.addActionListener(menuListener);
		itemFreeze.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.ALT_MASK));
		menuSimulation.add(itemFreeze);
		itemBoundaries = new JMenuItem();
		itemBoundaries.addActionListener(menuListener);
		menuSimulation.add(itemBoundaries);
		add(menuSimulation);

		menuShow = new JMenu();
		menuShow.setMnemonic(KeyEvent.VK_W);
		itemConsole = new JMenuItem();
		itemConsole.addActionListener(menuListener);
		menuShow.add(itemConsole);
		menuShow.addSeparator();
		itemAutoscale1 = new JMenuItem();
		itemAutoscale1
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
		itemAutoscale1.addActionListener(menuListener);
		menuShow.add(itemAutoscale1);
		itemAutoscale2 = new JMenuItem();
		itemAutoscale2
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
		itemAutoscale2.addActionListener(menuListener);
		menuShow.add(itemAutoscale2);
		itemFollow = new JMenuItem();
		itemFollow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
		itemFollow.addActionListener(menuListener);
		menuShow.add(itemFollow);
		menuShow.addSeparator();
		itemVelocities = new JCheckBoxMenuItem();
		itemVelocities.addActionListener(menuListener);
		menuShow.add(itemVelocities);
		itemForces = new JCheckBoxMenuItem();
		itemForces.addActionListener(menuListener);
		menuShow.add(itemForces);
		itemPretty = new JCheckBoxMenuItem();
		itemPretty.addActionListener(menuListener);
		menuShow.add(itemPretty);
		itemTags = new JCheckBoxMenuItem();
		itemTags.addActionListener(menuListener);
		menuShow.add(itemTags);
		itemGrid = new JCheckBoxMenuItem();
		itemGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		itemGrid.addActionListener(menuListener);
		menuShow.add(itemGrid);
		itemTracks = new JCheckBoxMenuItem();
		itemTracks.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
		itemTracks.addActionListener(menuListener);
		menuShow.add(itemTracks);
		menuShow.addSeparator();
		itemScreenshot = new JMenuItem();
		itemScreenshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.ALT_MASK));
		itemScreenshot.addActionListener(menuListener);
		menuShow.add(itemScreenshot);
		menuShow.addSeparator();
		itemLanguage = new JMenuItem();
		itemLanguage.addActionListener(menuListener);
		menuShow.add(itemLanguage);
		add(menuShow);

		menuControl = new JMenu();
		menuControl.setMnemonic(KeyEvent.VK_C);
		menuMouse = new JMenu();
		menuControl.add(menuMouse);
		itemByForce = new JRadioButtonMenuItem();
		itemByForce.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.SHIFT_MASK));
		itemByForce.setSelected(true);
		itemByForce.addActionListener(menuListener);
		menuMouse.add(itemByForce);
		buttonGroupMouse.add(itemByForce);
		itemByPlace = new JRadioButtonMenuItem();
		itemByPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.SHIFT_MASK));
		itemByPlace.setSelected(false);
		itemByPlace.addActionListener(menuListener);
		menuMouse.add(itemByPlace);
		buttonGroupMouse.add(itemByPlace);
		add(menuControl);

		menuHelp = new JMenu();
		menuHelp.setMnemonic(KeyEvent.VK_H);
		itemAbout = new JMenuItem();
		itemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK));
		menuHelp.add(itemAbout);
		itemAbout.addActionListener(menuListener);
		add(menuHelp);
	}

	public void applyLabels() {
		menuFile.setText(GUIStrings.MENU_FILE);
		itemOpen.setText(GUIStrings.MENU_OPEN);
		itemSave.setText(GUIStrings.MENU_SAVE);
		itemClear.setText(GUIStrings.MENU_CLEAR_ALL);
		itemExit.setText(GUIStrings.MENU_EXIT);

		menuEdit.setText(GUIStrings.MENU_EDIT);
		itemMouseSelect1.setText(GUIStrings.MENU_SELECT_SPRINGS);
		itemMouseSelect2.setText(GUIStrings.MENU_SELECT_PARTICLES);
		itemSelectAll.setText(GUIStrings.MENU_SELECT_ALL_PARTICLES);
		itemCoM.setText(GUIStrings.MENU_CENTER_OF_MASS);
		itemDelete.setText(GUIStrings.MENU_DELETE);
		itemFix.setText(GUIStrings.MENU_FIX);
		itemSnap.setText(GUIStrings.MENU_SNAP_TO_GRID);

		menuAdd.setText(GUIStrings.MENU_ADD);
		itemAdd1.setText(GUIStrings.MENU_ADD_PARTICLE);
		itemAdd2.setText(GUIStrings.MENU_ADD_SPRING);

		menuSimulation.setText(GUIStrings.MENU_SIMULATION);
		itemStart.setText(GUIStrings.MENU_START_PAUSE);
		itemSteps.setText(GUIStrings.MENU_FEW_STEPS);
		itemFreeze.setText(GUIStrings.MENU_NULLIFY_VELOCITIES);
		menuConsider.setText(GUIStrings.MENU_CONSIDER);
		itemCollisions1.setText(GUIStrings.MENU_PP_COLLISIONS);
		itemOuterForces.setText(GUIStrings.MENU_OUTER_FORCES);
		itemFriction.setText(GUIStrings.MENU_FRICTION);

		menuShow.setText(GUIStrings.MENU_SHOW);
		itemLanguage.setText(GUIStrings.MENU_LANGUAGE);
		itemBoundaries.setText(GUIStrings.MENU_SIMULATION_BOUNDS);
		itemConsole.setText(GUIStrings.MENU_CONSOLE);
		itemAutoscale1.setText(GUIStrings.MENU_ZOOM_TO_PARTICLES);
		itemAutoscale2.setText(GUIStrings.MENU_ZOOM_TO_BOUNDARIES);
		itemFollow.setText(GUIStrings.MENU_FOLLOW_PARTICLE);
		itemVelocities.setText(GUIStrings.MENU_VELOCITIES);
		itemForces.setText(GUIStrings.MENU_FORCES);
		itemPretty.setText(GUIStrings.MENU_PRETTY);
		itemTags.setText(GUIStrings.MENU_TAGS);
		itemGrid.setText(GUIStrings.MENU_GRID);
		itemTracks.setText(GUIStrings.MENU_PARTICLE_TRACKS);
		itemScreenshot.setText(GUIStrings.MENU_TAKE_SCREENSHOT);

		menuControl.setText(GUIStrings.MENU_CONTROL);
		menuMouse.setText(GUIStrings.MENU_MOUSE);
		itemByForce.setText(GUIStrings.MENU_CONTROL_BY_FORCE);
		itemByPlace.setText(GUIStrings.MENU_CONTROL_BY_PLACE);

		menuHelp.setText(GUIStrings.MENU_HELP);
		itemAbout.setText(GUIStrings.MENU_ABOUT);
	}

	public void refreshItems() {
		itemCollisions1.setSelected(Simulation.interactionProcessor.isUsePPCollisions());
		itemOuterForces.setSelected(Simulation.interactionProcessor.isUseExternalForces());
		itemFriction.setSelected(Simulation.interactionProcessor.isUseFriction());
		itemVelocities.setSelected(ParticleShape.drawVelocities);
		itemForces.setSelected(ParticleShape.drawForces);
		itemPretty.setSelected(ParticleShape.drawGradientParticles);
		itemTags.setSelected(ParticleShape.drawTags);
		itemGrid.setSelected(viewport.useGrid);
		itemTracks.setSelected(viewport.isDrawTracks());
		if (viewport.getMouseMode() == MouseMode.PARTICLE_SELECT)
			itemMouseSelect2.setSelected(true);
		else if (viewport.getMouseMode() == MouseMode.SPRING_SELECT)
			itemMouseSelect1.setSelected(true);
		else if (viewport.getMouseMode() == MouseMode.PARTICLE_ADD)
			itemAdd1.setSelected(true);
		else if (viewport.getMouseMode() == MouseMode.SPRING_ADD)
			itemAdd2.setSelected(true);
		else if (viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_ACCELERATION)
			itemByForce.setSelected(true);
		else if (viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE)
			itemByPlace.setSelected(true);
	}

}
