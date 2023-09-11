package test;

import javax.swing.SwingUtilities;

import gui.MainWindow;

public class Start {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainWindow();
			}
		});
	}
}