package org.ice1000.devkt.ui.swing.forms;

import org.ice1000.devkt.lang.PsiViewerImpl;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author ice1000
 * @see PsiViewerImpl
 */
public abstract class PsiViewer extends JDialog {
	protected JPanel mainPanel;
	protected JButton buttonClose;
	protected JScrollPane pane;
	protected JButton expandAll;
	protected JButton collapseAll;

	public PsiViewer(@Nullable Window owner) {
		super(owner);
	}
}
