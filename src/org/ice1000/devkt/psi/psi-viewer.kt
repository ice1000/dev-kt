package org.ice1000.devkt.psi

import org.ice1000.devkt.ui.PsiViewer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
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
		isModal = true
		rootPane.defaultButton = buttonClose
		pane.setViewportView(JTree(mapAst2Display(file)))
		buttonClose.addActionListener { dispose() }
		pack()
	}

	/**
	 * 缅怀一下天国的 Lice AST Viewer
	 * TODO make the second arg of [cutText] configurable
	 */
	private fun mapAst2Display(
			node: PsiElement,
			root: UINode = UINode("${cutText(node.text, 30)} => $node")): UINode = when {
		node.firstChild == null -> UINode(node)
		else -> root.apply {
			node.children.forEach { add(mapAst2Display(it)) }
		}
	}
}
