package org.ice1000.devkt.ui;

import org.ice1000.devkt.Kotlin;
import org.ice1000.devkt.psi.PsiViewerImpl;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author ice1000
 * @see PsiViewerImpl
 */
public abstract class PsiViewer extends JDialog {
	protected JPanel mainPanel;
	protected JButton buttonClose;
	protected JScrollPane pane;

	public static void main(String[] args) throws IOException {
		PsiViewer dialog = new PsiViewerImpl(Kotlin.INSTANCE.parse(new String(Files.readAllBytes(Paths.get("build.gradle.kts")))));
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}
}
