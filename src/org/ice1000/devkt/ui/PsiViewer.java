package org.ice1000.devkt.ui;

import javax.swing.*;

public class PsiViewer extends JDialog {
	private JPanel contentPane;
	private JButton buttonClose;

	public PsiViewer() {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonClose);
		buttonClose.addActionListener(e -> onOK());
	}

	private void onOK() {
		dispose();
	}

	public static void main(String[] args) {
		PsiViewer dialog = new PsiViewer();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}
}
