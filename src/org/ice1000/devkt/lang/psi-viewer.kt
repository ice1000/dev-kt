package org.ice1000.devkt.lang

import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.ui.PsiViewer
import org.jetbrains.kotlin.com.intellij.psi.*
import java.awt.Window
import javax.swing.JTree
import javax.swing.tree.*

typealias UINode = DefaultMutableTreeNode

/**
 * @author ice1000
 */
class PsiViewerImpl(file: PsiFile, owner: Window? = null) : PsiViewer(owner) {
	init {
		contentPane = mainPanel
		if (owner != null) setLocationRelativeTo(owner)
		title = "Psi Viewer"
		isModal = true
		rootPane.defaultButton = buttonClose
		val tree = JTree(mapAst2Display(file))
		pane.setViewportView(tree)
		expandAll.addActionListener { expandAll(tree, true) }
		collapseAll.addActionListener { expandAll(tree, false) }
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

	private fun expandAll(tree: JTree, expand: Boolean) {
		val root = tree.model.root as TreeNode
		expandAll(tree, TreePath(root), expand)
	}

	/**
	 * @return Whether an expandPath was called for the last node in the parent path
	 */
	private fun expandAll(tree: JTree, parent: TreePath, expand: Boolean): Boolean {
		val node = parent.lastPathComponent as TreeNode
		return if (node.childCount > 0) {
			var childExpandCalled = false
			val e = node.children()
			while (e.hasMoreElements()) {
				val n = e.nextElement() as TreeNode
				val path = parent.pathByAddingChild(n)
				childExpandCalled = expandAll(tree, path, expand) || childExpandCalled
			}

			if (!childExpandCalled) {
				if (expand) tree.expandPath(parent) else tree.collapsePath(parent)
			}
			true
		} else false
	}


	private fun prettyPrint(node: PsiElement) =
			"${cutText(node.text, GlobalSettings.psiViewerMaxCodeLength)} => ${node.javaClass.simpleName}(${node.nodeType})"
}
