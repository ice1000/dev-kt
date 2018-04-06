package org.ice1000.devkt.ui;

import javax.swing.*;
import java.awt.event.*;

public class Configuration extends JDialog {
	private JPanel mainPanel;
	private JButton buttonOK;
	private JButton buttonCancel;

	public Configuration() {
		setContentPane(mainPanel);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		buttonOK.addActionListener(e -> onOK());
		buttonCancel.addActionListener(e -> onCancel());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		mainPanel.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onOK() {
		// add your code here
		dispose();
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	public static void main(String[] args) {
		Configuration dialog = new Configuration();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}
}
