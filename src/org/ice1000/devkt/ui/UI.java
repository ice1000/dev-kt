package org.ice1000.devkt.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ice1000
 */
public abstract class UI {
	private JPanel mainPanel;
	protected JMenuBar menuBar;
	protected JTextPane editor;
	protected JToolBar toolbar;
	protected JLabel messageLabel;
	protected JLabel lineNumberLabel;

	public @NotNull JPanel getMainPanel() {
		return mainPanel;
	}
}
