package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.ui.Configuration
import java.awt.Window
import java.awt.event.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.KeyStroke

class ConfigurationImpl(parent: Window? = null) : Configuration(parent) {
	init {
		contentPane = mainPanel
		isModal = true
		getRootPane().defaultButton = buttonOK
		buttonOK.addActionListener { ok() }
		buttonCancel.addActionListener { dispose() }
		buttonReset.addActionListener { reset() }
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
		reset()
	}

	private fun reset() {
		backgroundImageField.text = GlobalSettings.backgroundImage.first
	}

	private fun ok() {
		GlobalSettings.backgroundImage = try {
			val path = backgroundImageField.text
			path to ImageIO.read(File(path))
		} catch (e: Exception) {
			return
		}
		dispose()
	}
}
