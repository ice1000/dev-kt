package org.ice1000.devkt.ui;

import javax.swing.*;

/**
 * @author ice1000
 */
public abstract class UI {
	public JPanel mainPanel;
	protected JMenuBar menuBar;
	protected JTextPane editor;
	protected JLabel messageLabel;
	protected JLabel lineNumberLabel;
	protected JPanel editorPanel;

	protected abstract void createUIComponents();
}
