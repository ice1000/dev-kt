package org.ice1000.devkt.ui.swing.forms;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public abstract class Configuration extends JDialog {
	protected JPanel mainPanel;
	protected JButton buttonOK;
	protected JButton buttonCancel;
	protected JButton buttonReset;
	protected JButton buttonApply;
	protected JTextField backgroundImageField;
	protected JSlider backgroundImageAlphaSlider;
	protected JComboBox<String> editorFontField;
	protected JComboBox<String> uiFontField;
	protected JSpinner fontSizeSpinner;
	protected JButton backgroundBrowse;
	protected JCheckBox useLexer;
	protected JCheckBox useParser;
	protected JTextField windowIconField;
	protected JButton browseWindowIcon;
	protected JTextField classNameField;
	protected JTextField jarNameField;

	public Configuration(@Nullable Window owner) {
		super(owner);
	}
}