package org.ice1000.devkt.psi

import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.ui.PsiViewer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtFile
import java.awt.Window
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

typealias UINode = DefaultMutableTreeNode

/**
 * @author ice1000
 */
class PsiViewerImpl(file: KtFile, owner: Window? = null) : PsiViewer(owner) {
	init {
		contentPane = mainPanel
		title = "Psi Viewer"
		isModal = true
		rootPane.defaultButton = buttonClose
		pane.setViewportView(JTree(mapAst2Display(file)))
		buttonClose.addActionListener { dispose() }
		pack()
	}

	/**
	 * 缅怀一下天国的 Lice AST Viewer
	 */
	private fun mapAst2Display(
			node: PsiElement,
			root: UINode = UINode(prettyPrint(node))
	): UINode = when {
		node.firstChild == null -> UINode(prettyPrint(node))
		else -> root.apply {
			generateSequence(node.firstChild) {
				it.nextSibling
			}.forEach { if (it !is PsiWhiteSpace) add(mapAst2Display(it)) }
		}
	}

	private fun prettyPrint(node: PsiElement) =
			"${cutText(node.text, GlobalSettings.psiViewerMaxCodeLength)} => ${node.javaClass.simpleName}(${node.nodeType})"
}
