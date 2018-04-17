package org.ice1000.devkt.ui.swing.dialogs

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.ice1000.devkt.ui.DevKtDocumentHandler
import org.ice1000.devkt.ui.DevKtIcons
import org.ice1000.devkt.ui.swing.AbstractUI
import java.awt.Component
import java.awt.Dimension
import java.awt.Insets
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

data class SearchResult(val start: Int, val end: Int)

open class FindDialog(
		uiImpl: AbstractUI,
		val document: DevKtDocumentHandler<*>) : JDialog() {
	companion object {
		val NO_REGEXP_CHARS = "\\{[(+*^\$.?|".toCharArray()
	}

	protected val searchResult = arrayListOf<SearchResult>()
	protected var currentIndex = 0

	private val mainPanel = JPanel()
	private val isMatchCase = JCheckBox()
	private val moveUp = JButton()
	private val moveDown = JButton()
	private val regex = JPanel()
	protected val isRegex = JCheckBox()
	protected val input = JTextField()
	protected val replaceInput = JTextField()
	protected val replace = JButton()
	protected val replaceAll = JButton()
	protected val separator = JSeparator()

	init {
		mainPanel.layout = GridLayoutManager(5, 1, Insets(0, 0, 0, 0), -1, -1)
		regex.layout = GridLayoutManager(1, 2, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(regex, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		isMatchCase.text = "Match Case"
		isMatchCase.setMnemonic('C')
		isMatchCase.displayedMnemonicIndex = 6
		regex.add(isMatchCase, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		isRegex.text = "regex"
		isRegex.setMnemonic('G')
		isRegex.displayedMnemonicIndex = 2
		regex.add(isRegex, GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		mainPanel.add(input, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		val panel1 = JPanel()
		panel1.layout = GridLayoutManager(1, 3, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(panel1, GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		moveUp.icon = DevKtIcons.MOVE_UP
		moveUp.text = ""
		panel1.add(moveUp, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		moveDown.icon = DevKtIcons.MOVE_DOWN
		moveDown.text = ""
		panel1.add(moveDown, GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer1 = Spacer()
		panel1.add(spacer1, GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		separator.isVisible = false
		mainPanel.add(separator, GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false))
		val panel2 = JPanel()
		panel2.layout = GridLayoutManager(2, 2, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(panel2, GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		replaceInput.isVisible = false
		panel2.add(replaceInput, GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		replace.text = "Replace"
		replace.setMnemonic('R')
		replace.displayedMnemonicIndex = 0
		replace.isVisible = false
		panel2.add(replace, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		replaceAll.text = "Replace all"
		replaceAll.setMnemonic('A')
		replaceAll.displayedMnemonicIndex = 8
		replaceAll.isVisible = false
		panel2.add(replaceAll, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))

		setLocationRelativeTo(uiImpl.mainPanel)

		contentPane = mainPanel
		title = "Find"
		isModal = true

		pack()

		moveUp.addActionListener { moveUp() }
		moveDown.addActionListener { moveDown() }
		isMatchCase.addActionListener { search() }
		isRegex.addActionListener { search() }
		input.document.addDocumentListener(object : DocumentListener {
			override fun changedUpdate(e: DocumentEvent?) = Unit                //不懂调用条件。。。
			override fun insertUpdate(e: DocumentEvent?) = removeUpdate(e)
			override fun removeUpdate(e: DocumentEvent?) = search()
		})
	}

	final override fun setLocationRelativeTo(c: Component?) = super.setLocationRelativeTo(c)
	final override fun pack() = super.pack()

	protected fun search() {
		searchResult.clear()
		document.selectionEnd = document.selectionStart

		val input = input.text
		val text = document.text
		val regex = if (isRegex.isSelected.not()) {                //FIXME stupid code 我太菜了
			var tempInput = input
			NO_REGEXP_CHARS.forEach {
				tempInput = tempInput.replace(it.toString(), "\\$it")
			}

			tempInput
		} else input

		try {
			Pattern.compile(
					regex,
					if (isMatchCase.isSelected.not()) Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE else 0
			).matcher(text).run {
				while (find()) {
					searchResult.add(SearchResult(start(), end()))
				}
			}

			select(0)
		} catch (e: PatternSyntaxException) {
			//TODO 做出提示
		}
	}

	protected open fun select(index: Int) {
		searchResult.getOrNull(index)?.let { (start, end) ->
			currentIndex = index
			document.selectionStart = start
			document.selectionEnd = end
		}
	}

	private fun moveUp() = select(currentIndex - 1)
	private fun moveDown() = select(currentIndex + 1)
}

class ReplaceDialog(
		uiImpl: AbstractUI, document: DevKtDocumentHandler<*>) :
		FindDialog(uiImpl, document) {
	init {
		title = "Replace"
		listOf<JComponent>(separator, replaceInput, replace, replaceAll).forEach {
			it.isVisible = true
		}

		pack()

		replace.addActionListener { replaceCurrent() }
		replaceAll.addActionListener { replaceAll() }
	}

	private fun replaceCurrent() {
		searchResult.getOrNull(currentIndex)?.run {
			document.resetTextTo(document.text.replaceRange(start until end, replaceInput.text))
		}
	}

	private fun replaceAll() {
		val findInput = input.text
		val replaceInput = replaceInput.text
		document.resetTextTo(if (isRegex.isSelected) {
			document.replaceText(Regex(findInput), replaceInput)
		} else document.text.replace(findInput, replaceInput))
	}
}
