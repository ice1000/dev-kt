package org.ice1000.devkt.ui;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public abstract class Configuration extends JDialog {
	protected JPanel mainPanel;
	protected JButton buttonOK;
	protected JButton buttonCancel;
	protected JButton buttonReset;
	protected JTextField backgroundImageField;

	public Configuration(@Nullable Window owner) {
		super(owner);
	}
}
