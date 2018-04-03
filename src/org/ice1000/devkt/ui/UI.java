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

	public @NotNull JPanel getMainPanel() {
		return mainPanel;
	}
}
