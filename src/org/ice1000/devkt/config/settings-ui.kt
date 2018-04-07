package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.allFonts
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.defaultFontName
import org.ice1000.devkt.ui.AbstractUI
import org.ice1000.devkt.ui.Configuration
import java.awt.Window
import java.awt.event.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*

class ConfigurationImpl(uiImpl: AbstractUI, parent: Window? = null) : Configuration(parent) {
	init {
		contentPane = mainPanel
		setLocationRelativeTo(uiImpl.mainPanel)
		title = "Settings"
		isModal = true
		getRootPane().defaultButton = buttonOK
		allFonts.forEach {
			editorFontField.addItem(it)
			uiFontField.addItem(it)
		}
		editorFontField.addItem(defaultFontName)
		uiFontField.addItem(defaultFontName)
		buttonOK.addActionListener { ok() }
		buttonApply.addActionListener { apply() }
		buttonCancel.addActionListener { dispose() }
		buttonReset.addActionListener { reset() }
		backgroundBrowse.addActionListener {
			val old = GlobalSettings.backgroundImage.first.let(::File)
			backgroundImageField.text = JFileChooser(if (old.exists()) old.parentFile else null).apply {
				showOpenDialog(mainPanel)
				fileSelectionMode = JFileChooser.FILES_ONLY
			}.selectedFile.absolutePath
		}
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
		editorFontField.selectedItem = GlobalSettings.monoFontName
		uiFontField.selectedItem = GlobalSettings.gothicFontName
		fontSizeSpinner.value = GlobalSettings.fontSize
	}

	private fun ok() {
		apply()
		dispose()
	}

	private fun apply() {
		with(GlobalSettings) {
			monoFontName = editorFontField.selectedItem.toString()
			gothicFontName = uiFontField.selectedItem.toString()
			(fontSizeSpinner.value as? Number)?.let { GlobalSettings.fontSize = it.toFloat() }
			backgroundImage = try {
				val path = backgroundImageField.text
				path to ImageIO.read(File(path))
			} catch (e: Exception) {
				return
			}
		}
	}
}
