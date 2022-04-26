package gui;

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

import gui.ViewportEvent.MouseMode;
import simulation.Simulation;

public class MainWindowMenu extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private JMenu menu;
	private ButtonGroup groupMouse;
	private MainWindowMenuEvent mwme;
	JRadioButtonMenuItem rbMenuItemMouse1p,rbMenuItemMouse1s,rbMenuItemMouse2p, rbMenuItemMouse4p,rbMenuItemMouse3p,rbMenuItemMouse2s;
	JMenuItem menuItemOpen, menuItemSave, menuItemExit, menuItemScene1, menuItemScene2, menuItemScene3, menuItemScene4,
			menuItemScene5, menuItemScene6, menuItemScene7, menuItemUndo, menuItemRedo;
	JMenuItem menuItemStart, menuItemStep, menuItemClear, menuItemFreeze, menuItemBoundaries;
	JMenuItem menuItemAutoscale, menuItemViewAll, menuItemSelectAll, menuItemAbout, menuItemMouseV, menuItemSaveScrn;
	JMenuItem menuItemFixParticle, menuItemConnect, menuItemFollowParticle, menuItemCOM, menuItemDelete, menuItemSnapToGrid, menuItemColorizeByCharge;
	JCheckBoxMenuItem cbMenuItem1pp, cbMenuItem1ss, cbMenuItem2, cbMenuItem3,  cbMenuItem3fr, cbMenuItem4, cbMenuItem5, cbMenuItem6, cbMenuItem7,
			cbMenuItem8, cbMenuItem9, cbMenuItem10;

	public MainWindowMenu() {

		groupMouse = new ButtonGroup();
		mwme = new MainWindowMenuEvent(this);

		menu = new JMenu("Файл");
		menu.setMnemonic(KeyEvent.VK_F);
		add(menu);

		menuItemOpen = new JMenuItem("Адкрыць");
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItemOpen.addActionListener(mwme);
		menu.add(menuItemOpen);

		menuItemSave = new JMenuItem("Захаваць");
		menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItemSave.addActionListener(mwme);
		menuItemSave.setEnabled(false);
		menu.add(menuItemSave);
		
		menu.addSeparator();

		menuItemClear = new JMenuItem("Ачысціць усё");
		menuItemClear.addActionListener(mwme);
		menuItemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ActionEvent.CTRL_MASK));
		menu.add(menuItemClear);

		menu.addSeparator();

		menuItemExit = new JMenuItem("Выйсці");
		menu.add(menuItemExit);
		menuItemExit.addActionListener(mwme);
		
		menu = new JMenu("Праўка");
		add(menu);
		
		rbMenuItemMouse1p = new JRadioButtonMenuItem("Вылучыць часціцы");
		rbMenuItemMouse1p.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK));
		rbMenuItemMouse1p.setSelected(false);
		rbMenuItemMouse1p.addActionListener(mwme);
		menu.add(rbMenuItemMouse1p);
		groupMouse.add(rbMenuItemMouse1p);

		rbMenuItemMouse1s = new JRadioButtonMenuItem(
				"Вылучыць пружыны (лін./загібн.)");
		rbMenuItemMouse1s.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK));
		rbMenuItemMouse1s.setSelected(false);
		rbMenuItemMouse1s.addActionListener(mwme);
		menu.add(rbMenuItemMouse1s);
		groupMouse.add(rbMenuItemMouse1s);

		menuItemSelectAll = new JMenuItem("Вылучыць усе часціцы");
		menuItemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuItemSelectAll.addActionListener(mwme);
		menu.add(menuItemSelectAll);

		menu.addSeparator();

		menuItemDelete = new JMenuItem("Выдаліць");
		menuItemDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menuItemDelete.addActionListener(mwme);
		menu.add(menuItemDelete);
		
		menuItemFixParticle = new JMenuItem("Зафіксаваць");
		menuItemFixParticle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		menuItemFixParticle.addActionListener(mwme);
		menu.add(menuItemFixParticle);

		menuItemCOM = new JMenuItem("Паказаць цэнтр мас");
		menuItemCOM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuItemCOM.addActionListener(mwme);
		menu.add(menuItemCOM);

		menuItemSnapToGrid = new JMenuItem("Прывязаць да сеткі");
		menuItemSnapToGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		menuItemSnapToGrid.addActionListener(mwme);
		menu.add(menuItemSnapToGrid);

		menu = new JMenu("Дадаць");
		add(menu);
		
		rbMenuItemMouse2p = new JRadioButtonMenuItem("Дадаць часціцу");
		rbMenuItemMouse2p.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
		rbMenuItemMouse2p.setSelected(false);
		rbMenuItemMouse2p.addActionListener(mwme);
		menu.add(rbMenuItemMouse2p);
		groupMouse.add(rbMenuItemMouse2p);

		rbMenuItemMouse2s = new JRadioButtonMenuItem("Дадаць пружыну");
		rbMenuItemMouse2s.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		rbMenuItemMouse2s.setSelected(false);
		rbMenuItemMouse2s.addActionListener(mwme);
		menu.add(rbMenuItemMouse2s);
		groupMouse.add(rbMenuItemMouse2s);

		menu = new JMenu("Сімуляцыя");
		menu.setMnemonic(KeyEvent.VK_S);
		add(menu);		

		menuItemStart = new JMenuItem("Старт/Паўза");
		menuItemStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
		menuItemStart.addActionListener(mwme);
		menu.add(menuItemStart);

		menuItemStep = new JMenuItem("Некалькі крокаў");
		menuItemStep.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
		menuItemStep.addActionListener(mwme);
		menu.add(menuItemStep);

		menu.addSeparator();
		
		JMenu submenu = new JMenu("Улічваць");
		menu.add(submenu);		

		cbMenuItem1pp = new JCheckBoxMenuItem("Сутыкненні часціц");
		cbMenuItem1pp.setMnemonic(KeyEvent.VK_C);
		cbMenuItem1pp.addActionListener(mwme);
		submenu.add(cbMenuItem1pp);

		cbMenuItem3 = new JCheckBoxMenuItem("Знешнія сілы");
		cbMenuItem3.setMnemonic(KeyEvent.VK_G);
		cbMenuItem3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		cbMenuItem3.addActionListener(mwme);
		submenu.add(cbMenuItem3);
		
		cbMenuItem3fr = new JCheckBoxMenuItem("Сілы трэння");
		cbMenuItem3fr.setMnemonic(KeyEvent.VK_F);
		cbMenuItem3fr.addActionListener(mwme);
		submenu.add(cbMenuItem3fr);
		
		menu.addSeparator();

		menuItemFreeze = new JMenuItem("Зануліць скорасці для ўсяго");
		menuItemFreeze.addActionListener(mwme);
		menuItemFreeze.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.ALT_MASK));
		menu.add(menuItemFreeze);

		menuItemBoundaries = new JMenuItem("Сценкі");
		menuItemBoundaries.addActionListener(mwme);
		menu.add(menuItemBoundaries);

		menu = new JMenu("Выгляд");
		menu.setMnemonic(KeyEvent.VK_V);
		add(menu);

		menuItemAutoscale = new JMenuItem("Маштаб па часціцах");
		menuItemAutoscale
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuItemAutoscale.addActionListener(mwme);
		menu.add(menuItemAutoscale);

		menuItemViewAll = new JMenuItem("Маштаб па стенках");
		menuItemViewAll
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuItemViewAll.addActionListener(mwme);
		menu.add(menuItemViewAll);

		menu.addSeparator();

		menuItemFollowParticle = new JMenuItem("Сачыць за часціцай");
		menuItemFollowParticle
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuItemFollowParticle.addActionListener(mwme);
		menu.add(menuItemFollowParticle);

		menu.addSeparator();

		cbMenuItem4 = new JCheckBoxMenuItem("Скорасці");
		cbMenuItem4.addActionListener(mwme);
		menu.add(cbMenuItem4);

		cbMenuItem5 = new JCheckBoxMenuItem("Сілы");
		cbMenuItem5.addActionListener(mwme);
		menu.add(cbMenuItem5);

		cbMenuItem7 = new JCheckBoxMenuItem("Градыенты на часціцах");
		cbMenuItem7.addActionListener(mwme);
		menu.add(cbMenuItem7);

		cbMenuItem8 = new JCheckBoxMenuItem("Пазнакі");
		cbMenuItem8.addActionListener(mwme);
		menu.add(cbMenuItem8);

		cbMenuItem9 = new JCheckBoxMenuItem("Сетка");
		cbMenuItem9.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		cbMenuItem9.addActionListener(mwme);
		menu.add(cbMenuItem9);
		
		cbMenuItem10 = new JCheckBoxMenuItem("Сляды ад часціц");
		cbMenuItem10.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
		cbMenuItem10.addActionListener(mwme);
		menu.add(cbMenuItem10);
		
		menu.addSeparator();
		
		menuItemSaveScrn = new JMenuItem("Захаваць выгляд сцэны");
		menuItemSaveScrn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.ALT_MASK));
		menuItemSaveScrn.addActionListener(mwme);
		menu.add(menuItemSaveScrn);

		menu = new JMenu("Маніпуляцыя");
		menu.setMnemonic(KeyEvent.VK_S);
		add(menu);
		
		submenu = new JMenu("Мыш");
		menu.add(submenu);

		rbMenuItemMouse3p = new JRadioButtonMenuItem("Падзейнічаць сілай на часціцу");
		rbMenuItemMouse3p.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_MASK));
		rbMenuItemMouse3p.setSelected(true);
		rbMenuItemMouse3p.addActionListener(mwme);
		submenu.add(rbMenuItemMouse3p);
		groupMouse.add(rbMenuItemMouse3p);

		rbMenuItemMouse4p = new JRadioButtonMenuItem("Зрушыць часціцу");
		rbMenuItemMouse4p.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_MASK));
		rbMenuItemMouse4p.setSelected(false);
		rbMenuItemMouse4p.addActionListener(mwme);
		submenu.add(rbMenuItemMouse4p);
		groupMouse.add(rbMenuItemMouse4p);
		
		menu = new JMenu("Дапамога");
		add(menu);
		
		menuItemAbout = new JMenuItem("Аб праграме");
		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK));
		menu.add(menuItemAbout);
		menuItemAbout.addActionListener(mwme);
	}
	
	void refreshItems() {
		cbMenuItem1pp.setSelected(Simulation.interactionProcessor.isUsePPCollisions());
		cbMenuItem3.setSelected(Simulation.interactionProcessor.isUseExternalForces());
		cbMenuItem3fr.setSelected(Simulation.interactionProcessor.isUseFriction());
		cbMenuItem4.setSelected(Viewport.drawVelocities);
		cbMenuItem5.setSelected(Viewport.drawForces);
		cbMenuItem7.setSelected(Viewport.drawGradientParticles);
		cbMenuItem8.setSelected(Viewport.drawTags);
		cbMenuItem9.setSelected(Viewport.useGrid);
		cbMenuItem10.setSelected(Viewport.isDrawTracks());
		if (Viewport.getMouseMode() == MouseMode.PARTICLE_SELECT)
			rbMenuItemMouse1p.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.SPRING_SELECT)
			rbMenuItemMouse1s.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.PARTICLE_ADD)
			rbMenuItemMouse2p.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.SPRING_ADD)
			rbMenuItemMouse2s.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_ACCELERATION)
			rbMenuItemMouse3p.setSelected(true);
		else if (Viewport.getMouseMode() == MouseMode.PARTICLE_MANIPULATION_COORDINATE)
			rbMenuItemMouse4p.setSelected(true);
	}
}
