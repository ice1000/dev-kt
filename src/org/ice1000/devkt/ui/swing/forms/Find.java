package org.ice1000.devkt.ui.swing.forms;

import javax.swing.*;

public abstract class Find extends JDialog {
    protected JPanel mainPanel;
    protected JCheckBox isMatchCase;
    protected JCheckBox isRegex;
    protected JTextField input;
    protected JButton moveUp;
    protected JButton moveDown;
    protected JPanel Regex;
    protected JTextField replaceInput;
    protected JButton replace;
    protected JButton replaceAll;
    protected JSeparator separator;
}
