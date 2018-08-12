package org.ice1000.devkt.ui.swing.dialogs

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.ice1000.devkt.ui.DevKtDocument
import org.ice1000.devkt.ui.swing.AbstractUI
import java.awt.Dimension
import java.awt.Insets
import javax.swing.*

class GoToLineDialog(uiImpl: AbstractUI, private val document: DevKtDocument<*>) : JDialog() {
	private val mainPanel = JPanel()
	private val lineColumn = JTextField()
	private val cancelButton = JButton()
	private val okButton = JButton()

	init {
		mainPanel.layout = GridLayoutManager(2, 3, Insets(8, 8, 8, 8), -1, -1)
		val panel1 = JPanel()
		panel1.layout = GridLayoutManager(2, 2, Insets(0, 0, 0, 0), -1, -1)
		mainPanel.add(panel1, GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		panel1.add(lineColumn, GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		val label1 = JLabel()
		label1.text = "[Line] [:column]:"
		panel1.add(label1, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val label2 = JLabel()
		label2.text = "Format: \"line\" or \"line:column\""
		panel1.add(label2, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		cancelButton.text = "Cancel"
		mainPanel.add(cancelButton, GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer1 = Spacer()
		mainPanel.add(spacer1, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		okButton.text = "OK"
		mainPanel.add(okButton, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		label1.labelFor = lineColumn

		setLocationRelativeTo(uiImpl.mainPanel)
		getRootPane().defaultButton = okButton
		contentPane = mainPanel
		title = "Go to Line/Column"
		isModal = true
		pack()
		okButton.addActionListener { ok() }
		cancelButton.addActionListener { dispose() }
		lineColumn.text = document.posToLineColumn(document.caretPosition).let { (line, column) ->
			"$line:$column"
		}
	}

	private fun ok() {
		val input = lineColumn.text.split(':')
		val line = input.firstOrNull()?.toIntOrNull() ?: return
		val column = input.getOrNull(1)?.toIntOrNull() ?: 1
		document.caretPosition = document.lineColumnToPos(line, column)
		dispose()
	}
}
