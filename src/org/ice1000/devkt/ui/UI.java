package org.ice1000.devkt.ui;

import org.ice1000.devkt.config.GlobalSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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

	private void createUIComponents() {
		mainPanel = new JPanel() {
			@Override public void paintComponent(Graphics g) {
				g.drawImage(GlobalSettings.INSTANCE.getWindowIcon().getSecond(), 0, 0, null);
			}
		};
	}
}
