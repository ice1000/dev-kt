package org.ice1000.devkt.ui;

import javax.swing.*;

/**
 * @author ice1000
 */
public abstract class UI {
	public JPanel mainPanel;
	public JLabel messageLabel;
	protected JMenuBar menuBar;
	protected JTextPane editor;
	protected JLabel lineNumberLabel;
	protected JScrollPane scrollPane;
	protected JButton memoryIndicator;

	protected abstract void createUIComponents();
}
