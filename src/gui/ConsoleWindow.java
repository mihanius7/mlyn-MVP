package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsoleWindow extends JFrame {
	private static JTextArea outputTextArea;
	private static JScrollPane scrollArea;
	
	public ConsoleWindow() {
		outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		outputTextArea.setFont(new Font("Monospaced", Font.BOLD, 12));
		outputTextArea.setFocusable(false);
		outputTextArea.setBackground(new Color(204, 204, 204));

		scrollArea = new JScrollPane();
		scrollArea.setViewportView(outputTextArea);
		getContentPane().add(scrollArea);
		
		outputTextArea.setBounds(4, getHeight() - 130 - 67, getWidth() - 26, 128);
		scrollArea.setBounds(outputTextArea.getBounds());
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
