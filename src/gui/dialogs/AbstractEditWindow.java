package gui.dialogs;

import javax.swing.JDialog;

public abstract class AbstractEditWindow extends JDialog {
	abstract void refreshContent();
	abstract void buildGUI();
	public void view() {
		refreshContent();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
