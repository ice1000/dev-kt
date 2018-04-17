package org.ice1000.devkt.ui.swing.dialogs

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.openapi.nodeType
import org.ice1000.devkt.openapi.util.cutText
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import java.awt.Insets
import java.awt.Window
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

typealias UINode = DefaultMutableTreeNode

/**
 * @author ice1000
 */
class PsiViewerImpl(file: PsiFile, owner: Window? = null) : JDialog(owner) {
	private val mainPanel = JPanel()
	private val buttonClose = JButton()
	private val pane = JScrollPane()
	private val expandAll = JButton()
	private val collapseAll = JButton()

	init {
		mainPanel.layout = GridLayoutManager(2, 4, Insets(10, 10, 10, 10), -1, -1)
		mainPanel.add(pane, GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false))
		val spacer1 = Spacer()
		mainPanel.add(spacer1, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		buttonClose.text = "Close"
		buttonClose.setMnemonic('C')
		buttonClose.displayedMnemonicIndex = 0
		mainPanel.add(buttonClose, GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		expandAll.text = "Expand all"
		expandAll.setMnemonic('E')
		expandAll.displayedMnemonicIndex = 0
		mainPanel.add(expandAll, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		collapseAll.text = "Collapse all"
		collapseAll.setMnemonic('A')
		collapseAll.displayedMnemonicIndex = 9
		mainPanel.add(collapseAll, GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))

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
