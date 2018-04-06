package org.ice1000.devkt.ui;

import org.ice1000.devkt.psi.PsiViewerImpl;
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

	public PsiViewer(@Nullable Window owner) {
		super(owner);
	}
}
