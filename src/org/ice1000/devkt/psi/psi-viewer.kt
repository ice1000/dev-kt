package org.ice1000.devkt.psi

import org.ice1000.devkt.ui.PsiViewer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

typealias UINode = DefaultMutableTreeNode

/**
 * @author ice1000
 */
class PsiViewerImpl(file: KtFile) : PsiViewer() {
	init {
		contentPane = mainPanel
		isModal = true
		rootPane.defaultButton = buttonClose
		pane.setViewportView(JTree(mapAst2Display(file)))
		buttonClose.addActionListener { dispose() }
	}

	/**
	 * 缅怀一下天国的 Lice AST Viewer
	 */
	private fun mapAst2Display(
			node: PsiElement, root: UINode = UINode("${node.text} => $node")): UINode = when {
		node.firstChild == null -> UINode(node)
		else -> root.apply {
			node.children.forEach { add(mapAst2Display(it)) }
		}
	}
}
