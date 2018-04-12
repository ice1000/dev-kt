package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.allFonts
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.defaultFontName
import org.ice1000.devkt.ui.swing.AbstractUI
import org.ice1000.devkt.ui.swing.DevKtFrame
import org.ice1000.devkt.ui.swing.Configuration
import java.awt.Font
import java.awt.event.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*

class ConfigurationImpl(private val uiImpl: AbstractUI, parent: DevKtFrame? = null) : Configuration(parent) {
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
		editorFontField.addItemListener {
			parent?.ui?.editorFont = Font(it.item.toString(), Font.PLAIN, 16)
					.deriveFont(GlobalSettings.fontSize)
		}
		uiFontField.model = DefaultComboBoxModel(allFonts)
		buttonOK.addActionListener { ok() }
		buttonApply.addActionListener { apply() }
		buttonCancel.addActionListener { dispose() }
		buttonReset.addActionListener { reset() }
		backgroundBrowse.addActionListener {
			val old = GlobalSettings.backgroundImage.first.let(::File)
			backgroundImageField.text = JFileChooser(if (old.exists()) old.parentFile else null).apply {
				showOpenDialog(mainPanel)
				fileSelectionMode = JFileChooser.FILES_ONLY
				// selectedFile will be return null if JFileChooser was canceled
			}.selectedFile?.absolutePath.orEmpty()
		}
		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				dispose()
			}
		})
		mainPanel.registerKeyboardAction({ dispose() },
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		doNothingOnClose
		pack()
		reset()
	}

	private fun reset() {
		backgroundImageField.text = GlobalSettings.backgroundImage.first
		editorFontField.selectedItem = GlobalSettings.monoFontName
		uiFontField.selectedItem = GlobalSettings.gothicFontName
		fontSizeSpinner.value = GlobalSettings.fontSize
		backgroundImageAlphaSlider.value = GlobalSettings.backgroundAlpha
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
			(backgroundImageAlphaSlider.value as? Number)?.let { GlobalSettings.backgroundAlpha = it.toInt() }
			monoFontName.apply {
				(parent as? DevKtFrame)?.let {
					it.ui.editorFont = Font(this, Font.PLAIN, GlobalSettings.fontSize.toInt())
				}
			}
			backgroundImage = backgroundImageField.text.let {
				try {
					it to ImageIO.read(File(it))
				} catch (e: Exception) {
					if (!it.isEmpty()) {
						JOptionPane.showMessageDialog(contentPane,
								"Image is invalid!",
								"Message",
								JOptionPane.ERROR_MESSAGE)
					}
					it to null
				}
			}
		}
		uiImpl.restart()
	}
}
