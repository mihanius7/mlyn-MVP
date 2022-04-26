package gui.editing;

import gui.Viewport;
import simulation.Simulation;
import simulation.components.Boundaries;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

public class EditBoundariesWindow extends AbstractEditWindow implements ActionListener {

	private static final long serialVersionUID = 1860690546230167889L;
	private JTextField field1, field2, field3, field4;
	private JCheckBox checkBox1, checkBox2, checkBox3, checkBox4;
	private Boundaries boundaries = Simulation.getContent().getBoundaries();

	public EditBoundariesWindow() {
		buildGUI();
		view();
	}

	void buildGUI() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(380, 192);
		setTitle("Рэдагаваць сценкі");

		JLabel label1 = new JLabel("Левая мяжа, м");
		label1.setBounds(16, 8, 180, 20);
		getContentPane().add(label1);

		field1 = new JTextField();
		field1.setBounds(205, 8, 50, label1.getHeight());
		getContentPane().add(field1);

		JLabel label2 = new JLabel("Правая мяжа, м");
		label2.setBounds(16, label1.getY() + label1.getHeight() + 8, 180, 20);
		getContentPane().add(label2);

		field2 = new JTextField();
		field2.setBounds(205, 36, 50, label2.getHeight());
		getContentPane().add(field2);

		JLabel label3 = new JLabel("Ніжняя мяжа, м");
		label3.setBounds(16, label2.getY() + label2.getHeight() + 8, 180, 20);
		getContentPane().add(label3);

		field3 = new JTextField();
		field3.setBounds(205, 64, 50, label3.getHeight());
		getContentPane().add(field3);

		JLabel label4 = new JLabel("Верхняя мяжа, м");
		label4.setBounds(16, label3.getY() + label3.getHeight() + 8, 180, 20);
		getContentPane().add(label4);

		field4 = new JTextField();
		field4.setBounds(205, 92, 50, label4.getHeight());
		getContentPane().add(field4);

		JButton button1 = new JButton("Ужыць");
		button1.setBounds(250, 124, 116, 24);
		button1.addActionListener(this);
		getContentPane().add(button1);

		Container c = getContentPane();
		c.setLayout(null);

		checkBox1 = new JCheckBox("\u0423\u043A\u043B\u044E\u0447\u0430\u043D\u0430");
		checkBox1.setHorizontalAlignment(SwingConstants.LEFT);
		checkBox1.setBounds(263, 6, 103, 25);
		getContentPane().add(checkBox1);

		checkBox2 = new JCheckBox("\u0423\u043A\u043B\u044E\u0447\u0430\u043D\u0430");
		checkBox2.setHorizontalAlignment(SwingConstants.LEFT);
		checkBox2.setBounds(263, 34, 103, 25);
		getContentPane().add(checkBox2);

		checkBox3 = new JCheckBox("\u0423\u043A\u043B\u044E\u0447\u0430\u043D\u0430");
		checkBox3.setHorizontalAlignment(SwingConstants.LEFT);
		checkBox3.setBounds(263, 62, 103, 25);
		getContentPane().add(checkBox3);

		checkBox4 = new JCheckBox("\u0423\u043A\u043B\u044E\u0447\u0430\u043D\u0430");
		checkBox4.setHorizontalAlignment(SwingConstants.LEFT);
		checkBox4.setBounds(263, 90, 103, 25);
		getContentPane().add(checkBox4);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		NumberFormat nf = NumberFormat.getInstance();
		try {
			boundaries.setBounds(nf.parse(field1.getText()).doubleValue(), nf.parse(field2.getText()).doubleValue(),
					nf.parse(field4.getText()).doubleValue(), nf.parse(field3.getText()).doubleValue());
			boundaries.setUseLeft(checkBox1.isSelected());
			boundaries.setUseRight(checkBox2.isSelected());
			boundaries.setUseBottom(checkBox3.isSelected());
			boundaries.setUseUpper(checkBox4.isSelected());
			Viewport.scaleToBoundaries();
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "Увядзіце лічбу прывільна!", "Атрыманне лічбы...",
					JOptionPane.WARNING_MESSAGE);
		} catch (ParseException e2) {
			JOptionPane.showMessageDialog(null, "Памылка падчас распазнання лічбы!", "Лакалізацыя лічбы...",
					JOptionPane.WARNING_MESSAGE);
		}
		refreshContent();
	}

	@Override
	void refreshContent() {
		field1.setText(String.format("%.2f", boundaries.getLeft()));
		checkBox1.setSelected(boundaries.isUseLeft());
		field2.setText(String.format("%.2f", boundaries.getRight()));
		checkBox2.setSelected(boundaries.isUseRight());
		field3.setText(String.format("%.2f", boundaries.getBottom()));
		checkBox3.setSelected(boundaries.isUseBottom());
		field4.setText(String.format("%.2f", boundaries.getUpper()));
		checkBox4.setSelected(boundaries.isUseUpper());
	}
}
