package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.ui.Configuration
import java.awt.event.*
import javax.swing.*

class ConfigurationImpl : Configuration() {
	init {
		contentPane = mainPanel
		isModal = true
		getRootPane().defaultButton = buttonOK
		buttonOK.addActionListener { onOK() }
		buttonCancel.addActionListener { dispose() }
		doNothingOnClose
		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				dispose()
			}
		})
		mainPanel.registerKeyboardAction({ dispose() },
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		pack()
	}

	private fun onOK() {
	}
}
