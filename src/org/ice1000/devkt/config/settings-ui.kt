package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.allFonts
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.defaultFontName
import org.ice1000.devkt.ui.AbstractUI
import org.ice1000.devkt.ui.Configuration
import org.ice1000.devkt.ui.DevKtFrame
import java.awt.*
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

		editorFontField.addItemListener {
			(parent as? DevKtFrame)?.apply {
				this.ui.editorFont = (Font(it.item.toString(), Font.PLAIN, GlobalSettings.fontSize.toInt()))
			}
		}

		backgroundImageOpacitySlider.apply {
			minimum = 0
			maximum = 255
			value = GlobalSettings.backgroundAlpha
		}
		backgroundImageOpacitySlider.addChangeListener {
			GlobalSettings.backgroundAlpha = (it.source as JSlider).value
			(parent as? DevKtFrame)?.apply {
				this.ui.mainPanel.repaint()
			}
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
	}
}
