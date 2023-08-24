package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ConsoleWindow extends JFrame {
	private static final int VIEW_WIDTH = 800;
	private static final int VIEW_HEIGHT = 512;
	private static JTextArea outputTextArea;
	private static JScrollPane scrollArea;
	
	public ConsoleWindow() {
		
		outputTextArea = new JTextArea(16, 58);
		outputTextArea.setLineWrap(true);
		outputTextArea.setFont(new Font("Monospaced", Font.BOLD, 12));
		outputTextArea.setBackground(new Color(204, 204, 204));
		outputTextArea.setEditable(false);

		scrollArea = new JScrollPane(outputTextArea);
		scrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		add(scrollArea);
		
		setTitle("Simulation Messages");

		setBounds(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
		setVisible(false);		
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
}
