package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.ui.AbstractUI
import org.ice1000.devkt.ui.Configuration
import java.awt.Window
import java.awt.event.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.KeyStroke

class ConfigurationImpl(private val uiImpl: AbstractUI, parent: Window? = null) : Configuration(parent) {
	init {
		contentPane = mainPanel
		title = "Settings"
		isModal = true
		getRootPane().defaultButton = buttonOK
		buttonOK.addActionListener { ok() }
		buttonApply.addActionListener { apply() }
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
		editorFontField.text = GlobalSettings.monoFontName
		uiFontField.text = GlobalSettings.gothicFontName
		fontSizeSpinner.value = GlobalSettings.fontSize
	}

	private fun ok() {
		apply()
		dispose()
	}

	private fun apply() {
		with(GlobalSettings) {
			monoFontName = editorFontField.text
			gothicFontName = uiFontField.text
			(fontSizeSpinner.value as? Number)?.let { GlobalSettings.fontSize = it.toFloat() }
			backgroundImage = try {
				val path = backgroundImageField.text
				path to ImageIO.read(File(path))
			} catch (e: Exception) {
				return
			}
		}
		uiImpl.reloadSettings()
	}
}
