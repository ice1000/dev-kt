package org.ice1000.devkt.ui.swing.dialogs

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.ice1000.devkt.ui.*
import org.ice1000.devkt.ui.swing.AbstractUI
import java.awt.Component
import java.awt.Dimension
import java.awt.Insets
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

abstract class FindUI : JDialog() {
	protected val mainPanel: JPanel = JPanel()
	protected val isMatchCase: JCheckBox = JCheckBox()
	protected val moveUp: JButton = JButton()
	protected val moveDown: JButton = JButton()
	protected val isRegex: JCheckBox = JCheckBox()
	protected val findInput: JTextField = JTextField()
	protected val replaceInput: JTextField = JTextField()
	protected val replace: JButton = JButton()
	protected val replaceAll: JButton = JButton()
	protected val separator: JSeparator = JSeparator()
	protected val findCount: JLabel = JLabel()

	init {
		mainPanel.layout = GridLayoutManager(5, 1, Insets(0, 0, 0, 0), -1, -1)
		val panel1 = JPanel()
		panel1.layout = GridLayoutManager(1, 2, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(panel1, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		isMatchCase.text = "Match Case"
		isMatchCase.setMnemonic('C')
		isMatchCase.displayedMnemonicIndex = 6
		panel1.add(isMatchCase, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		isRegex.text = "Regex"
		isRegex.setMnemonic('G')
		isRegex.displayedMnemonicIndex = 2
		panel1.add(isRegex, GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		mainPanel.add(findInput, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		val panel2 = JPanel()
		panel2.layout = GridLayoutManager(1, 5, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(panel2, GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		moveUp.icon = DevKtIcons.MOVE_UP
		moveUp.text = ""
		panel2.add(moveUp, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		moveDown.icon = DevKtIcons.MOVE_DOWN
		moveDown.text = ""
		panel2.add(moveDown, GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer1 = Spacer()
		panel2.add(spacer1, GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		findCount.text = "0/0"
		panel2.add(findCount, GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer2 = Spacer()
		panel2.add(spacer2, GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		separator.isVisible = false
		mainPanel.add(separator, GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false))
		val panel3 = JPanel()
		panel3.layout = GridLayoutManager(2, 2, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(panel3, GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		replaceInput.isVisible = false
		panel3.add(replaceInput, GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		replace.text = "Replace"
		replace.setMnemonic('R')
		replace.displayedMnemonicIndex = 0
		replace.isVisible = false
		panel3.add(replace, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		replaceAll.text = "Replace all"
		replaceAll.setMnemonic('A')
		replaceAll.displayedMnemonicIndex = 8
		replaceAll.isVisible = false
		panel3.add(replaceAll, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
	}
}

open class FindDialogImpl(
		uiImpl: AbstractUI,
		override val document: DevKtDocumentHandler<*>) : FindUI(), IFind {
	protected open val bundle get() = FindDataBundle(findInput.text, isMatchCase.isSelected, isRegex.isSelected)
	override val searchResult = arrayListOf<SearchResult>()
	override var currentIndex = 0

	init {
		setLocationRelativeTo(uiImpl.mainPanel)

		contentPane = mainPanel
		title = "Find"
		isModal = true

		pack()

		moveUp.addActionListener { moveUp() }
		moveDown.addActionListener { moveDown() }
		isMatchCase.addActionListener { search(bundle) }
		isRegex.addActionListener { search(bundle) }
		findInput.document.addDocumentListener(object : DocumentListener {
			override fun changedUpdate(e: DocumentEvent?) = Unit                //不懂调用条件。。。
			override fun insertUpdate(e: DocumentEvent?) = removeUpdate(e)
			override fun removeUpdate(e: DocumentEvent?) = search(bundle)
		})
	}

	override fun update() {
		findCount.text = "${currentIndex.takeIf { searchResult.size != 0 }?.plus(1) ?: 0}/${searchResult.size}"
	}

	final override fun setLocationRelativeTo(c: Component?) = super.setLocationRelativeTo(c)
	final override fun pack() = super.pack()
}

class ReplaceDialogImpl(
		uiImpl: AbstractUI, document: DevKtDocumentHandler<*>) :
		FindDialogImpl(uiImpl, document), IReplace {

	override val bundle
		get() = super.bundle.apply {
			replaceInput = this@ReplaceDialogImpl.replaceInput.text
		}

	init {
		title = "Replace"
		listOf<JComponent>(separator, replaceInput, replace, replaceAll).forEach {
			it.isVisible = true
		}

		pack()

		replace.addActionListener { replaceCurrent(bundle) }
		replaceAll.addActionListener { replaceAll(bundle) }
	}
}
