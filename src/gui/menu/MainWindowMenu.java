package gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import gui.Viewport;
import gui.ViewportEvent.MouseMode;
import gui.lang.GUIStrings;
import simulation.Simulation;

public class MainWindowMenu extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private JMenu menuFile, menuEdit, menuAdd, menuSimulation, menuShow, menuControl, menuHelp, menuConsider, menuMouse;
	private ButtonGroup buttonGroupMouse;
	private MainWindowMenuEvent listener;
	JRadioButtonMenuItem itemMouseSelect2, itemMouseSelect1, itemAdd1, itemByPlace, itemByForce,
			itemAdd2;
	JMenuItem itemOpen, itemSave, itemExit, menuItemScene1, menuItemScene2, menuItemScene3, menuItemScene4,
			menuItemScene5, menuItemScene6, menuItemScene7, menuItemUndo, menuItemRedo;
	JMenuItem itemStart, itemSteps, itemClear, menuItemFreeze, itemBoundaries;
	JMenuItem itemAutoscale1, itemAutoscale2, itemSelectAll, itemAbout, menuItemMouseV, itemScreenshot;
	JMenuItem itemFix, menuItemConnect, itemFollow, itemCoM, itemDelete,
			itemSnap, menuItemColorizeByCharge;
	JCheckBoxMenuItem itemCollisions1, cbMenuItem1ss, cbMenuItem2, itemOuterForces, itemFriction, itemVelocities, itemForces,
			cbMenuItem6, itemPretty, itemTags, itemGrid, itemTracks;

	public MainWindowMenu() {

		buttonGroupMouse = new ButtonGroup();
		listener = new MainWindowMenuEvent(this);

		menuFile = new JMenu();
		menuFile.setMnemonic(KeyEvent.VK_F);

		add(menuFile);
		itemOpen = new JMenuItem();
		itemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		itemOpen.addActionListener(listener);
		menuFile.add(itemOpen);
		itemSave = new JMenuItem();
		itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		itemSave.addActionListener(listener);
		itemSave.setEnabled(false);
		menuFile.add(itemSave);
		menuFile.addSeparator();
		itemClear = new JMenuItem();
		itemClear.addActionListener(listener);
		itemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ActionEvent.CTRL_MASK));
		menuFile.add(itemClear);
		menuFile.addSeparator();
		itemExit = new JMenuItem();
		menuFile.add(itemExit);
		itemExit.addActionListener(listener);

		menuEdit = new JMenu();
		add(menuEdit);
		itemMouseSelect2 = new JRadioButtonMenuItem();
		itemMouseSelect2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK));
		itemMouseSelect2.setSelected(false);
		itemMouseSelect2.addActionListener(listener);
		menuEdit.add(itemMouseSelect2);
		buttonGroupMouse.add(itemMouseSelect2);
		itemMouseSelect1 = new JRadioButtonMenuItem();
		itemMouseSelect1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK));
		itemMouseSelect1.setSelected(false);
		itemMouseSelect1.addActionListener(listener);
		menuEdit.add(itemMouseSelect1);
		buttonGroupMouse.add(itemMouseSelect1);
		itemSelectAll = new JMenuItem();
		itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		itemSelectAll.addActionListener(listener);
		menuEdit.add(itemSelectAll);
		menuEdit.addSeparator();
		itemDelete = new JMenuItem();
		itemDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		itemDelete.addActionListener(listener);
		menuEdit.add(itemDelete);
		itemFix = new JMenuItem();
		itemFix.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		itemFix.addActionListener(listener);
		menuEdit.add(itemFix);
		itemCoM = new JMenuItem();
		itemCoM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		itemCoM.addActionListener(listener);
		menuEdit.add(itemCoM);
		itemSnap = new JMenuItem();
		itemSnap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		itemSnap.addActionListener(listener);
		menuEdit.add(itemSnap);

		menuAdd = new JMenu();
		add(menuAdd);
		itemAdd1 = new JRadioButtonMenuItem();
		itemAdd1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
		itemAdd1.setSelected(false);
		itemAdd1.addActionListener(listener);
		menuAdd.add(itemAdd1);
		buttonGroupMouse.add(itemAdd1);
		itemAdd2 = new JRadioButtonMenuItem();
		itemAdd2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		itemAdd2.setSelected(false);
		itemAdd2.addActionListener(listener);
		menuAdd.add(itemAdd2);
		buttonGroupMouse.add(itemAdd2);

		menuSimulation = new JMenu();
		menuSimulation.setMnemonic(KeyEvent.VK_S);
		add(menuSimulation);
		itemStart = new JMenuItem();
		itemStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
		itemStart.addActionListener(listener);
		menuSimulation.add(itemStart);
		itemSteps = new JMenuItem();
		itemSteps.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
		itemSteps.addActionListener(listener);
		menuSimulation.add(itemSteps);
		menuSimulation.addSeparator();
		menuConsider = new JMenu();
		menuSimulation.add(menuConsider);
		itemCollisions1 = new JCheckBoxMenuItem();
		itemCollisions1.setMnemonic(KeyEvent.VK_C);
		itemCollisions1.addActionListener(listener);
		menuConsider.add(itemCollisions1);
		itemOuterForces = new JCheckBoxMenuItem();
		itemOuterForces.setMnemonic(KeyEvent.VK_G);
		itemOuterForces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		itemOuterForces.addActionListener(listener);
		menuConsider.add(itemOuterForces);
		itemFriction = new JCheckBoxMenuItem();
		itemFriction.setMnemonic(KeyEvent.VK_F);
		itemFriction.addActionListener(listener);
		menuConsider.add(itemFriction);
		menuSimulation.addSeparator();
		menuItemFreeze = new JMenuItem();
		menuItemFreeze.addActionListener(listener);
		menuItemFreeze.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.ALT_MASK));
		menuSimulation.add(menuItemFreeze);
		itemBoundaries = new JMenuItem();
		itemBoundaries.addActionListener(listener);
		menuSimulation.add(itemBoundaries);

		menuShow = new JMenu();
		menuShow.setMnemonic(KeyEvent.VK_V);
		add(menuShow);
		itemAutoscale1 = new JMenuItem();
		itemAutoscale1
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		itemAutoscale1.addActionListener(listener);
		menuShow.add(itemAutoscale1);
		itemAutoscale2 = new JMenuItem();
		itemAutoscale2
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		itemAutoscale2.addActionListener(listener);
		menuShow.add(itemAutoscale2);
		menuShow.addSeparator();
		itemFollow = new JMenuItem();
		itemFollow
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		itemFollow.addActionListener(listener);
		menuShow.add(itemFollow);
		menuShow.addSeparator();
		itemVelocities = new JCheckBoxMenuItem();
		itemVelocities.addActionListener(listener);
		menuShow.add(itemVelocities);
		itemForces = new JCheckBoxMenuItem();
		itemForces.addActionListener(listener);
		menuShow.add(itemForces);
		itemPretty = new JCheckBoxMenuItem();
		itemPretty.addActionListener(listener);
		menuShow.add(itemPretty);
		itemTags = new JCheckBoxMenuItem();
		itemTags.addActionListener(listener);
		menuShow.add(itemTags);
		itemGrid = new JCheckBoxMenuItem();
		itemGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		itemGrid.addActionListener(listener);
		menuShow.add(itemGrid);
		itemTracks = new JCheckBoxMenuItem();
		itemTracks.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
		itemTracks.addActionListener(listener);
		menuShow.add(itemTracks);
		menuShow.addSeparator();
		itemScreenshot = new JMenuItem();
		itemScreenshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.ALT_MASK));
		itemScreenshot.addActionListener(listener);
		menuShow.add(itemScreenshot);

		menuControl = new JMenu();
		menuControl.setMnemonic(KeyEvent.VK_S);
		add(menuControl);
		menuMouse = new JMenu();
		menuControl.add(menuMouse);
		itemByForce = new JRadioButtonMenuItem();
		itemByForce.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_MASK));
		itemByForce.setSelected(true);
		itemByForce.addActionListener(listener);
		menuMouse.add(itemByForce);
		buttonGroupMouse.add(itemByForce);
		itemByPlace = new JRadioButtonMenuItem();
		itemByPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_MASK));
		itemByPlace.setSelected(false);
		itemByPlace.addActionListener(listener);
		menuMouse.add(itemByPlace);
		buttonGroupMouse.add(itemByPlace);

		menuHelp = new JMenu();
		add(menuHelp);
		itemAbout = new JMenuItem();
		itemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK));
		menuHelp.add(itemAbout);
		itemAbout.addActionListener(listener);
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
		menuItemFreeze.setText(GUIStrings.MENU_NULLIFY_VELOCITIES);
		menuConsider.setText(GUIStrings.MENU_CONSIDER);
		itemCollisions1.setText(GUIStrings.MENU_PP_COLLISIONS);
		itemOuterForces.setText(GUIStrings.MENU_OUTER_FORCES);
		itemFriction.setText(GUIStrings.MENU_FRICTION);

		menuShow.setText(GUIStrings.MENU_SHOW);
		itemBoundaries.setText(GUIStrings.MENU_SIMULATION_BOUNDS);
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
		itemVelocities.setSelected(Viewport.drawVelocities);
		itemForces.setSelected(Viewport.drawForces);
		itemPretty.setSelected(Viewport.drawGradientParticles);
		itemTags.setSelected(Viewport.drawTags);
		itemGrid.setSelected(Viewport.useGrid);
		itemTracks.setSelected(Viewport.isDrawTracks());
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_SELECT)
			itemMouseSelect2.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.SPRING_SELECT)
			itemMouseSelect1.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.PARTICLE_ADD)
			itemAdd1.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.SPRING_ADD)
			itemAdd2.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_ACCELERATION)
			itemByForce.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE)
			itemByPlace.setSelected(true);
	}

}
