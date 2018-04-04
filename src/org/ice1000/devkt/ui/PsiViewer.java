package org.ice1000.devkt.ui;

import org.ice1000.devkt.psi.PsiViewerImpl;

import javax.swing.*;

/**
 * @author ice1000
 * @see PsiViewerImpl
 */
public abstract class PsiViewer extends JDialog {
	protected JPanel mainPanel;
	protected JButton buttonClose;
	protected JScrollPane pane;
}
