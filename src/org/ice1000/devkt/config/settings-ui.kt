package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.allFonts
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.defaultFontName
import org.ice1000.devkt.ui.swing.AbstractUI
import org.ice1000.devkt.ui.swing.DevKtFrame
import org.ice1000.devkt.ui.swing.forms.Configuration
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
		backgroundBrowse.addActionListener(browseActionOf(GlobalSettings.backgroundImage, backgroundImageField))
		windowIconField.addActionListener(browseActionOf(GlobalSettings.windowIcon, windowIconField))
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

	private fun browseActionOf(oldPair: Pair<String, *>, textField: JTextField) = ActionListener {
		val old = oldPair.first.let(::File)
		textField.text = JFileChooser(old.parentFile?.takeIf { old.exists() }).apply {
			showOpenDialog(mainPanel)
			fileSelectionMode = JFileChooser.FILES_ONLY
			// selectedFile will be return null if JFileChooser was canceled
		}.selectedFile?.absolutePath.orEmpty()
	}

	private fun reset() = with(GlobalSettings) {
		backgroundImageField.text = backgroundImage.first
		windowIconField.text = windowIcon.first
		editorFontField.selectedItem = monoFontName
		uiFontField.selectedItem = gothicFontName
		fontSizeSpinner.value = fontSize
		backgroundImageAlphaSlider.value = backgroundAlpha
		useLexer.isSelected = highlightTokenBased
		useParser.isSelected = highlightSemanticBased
	}

	private fun ok() {
		apply()
		dispose()
	}

	private fun apply() = with(receiver = GlobalSettings) {
		monoFontName = editorFontField.selectedItem.toString()
		gothicFontName = uiFontField.selectedItem.toString()
		(fontSizeSpinner.value as? Number)?.let { GlobalSettings.fontSize = it.toFloat() }
		(backgroundImageAlphaSlider.value as? Number)?.let { GlobalSettings.backgroundAlpha = it.toInt() }
		monoFontName.apply {
			(parent as? DevKtFrame)?.let {
				it.ui.editorFont = Font(this, Font.PLAIN, GlobalSettings.fontSize.toInt())
			}
		}
		highlightTokenBased = useLexer.isSelected
		highlightSemanticBased = useParser.isSelected
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
		windowIcon = windowIconField.text.let {
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
		uiImpl.restart()
	}
}
