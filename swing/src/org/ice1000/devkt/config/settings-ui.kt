package org.ice1000.devkt.config

import charlie.gensokyo.doNothingOnClose
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import org.ice1000.devkt.DevKtFontManager.allFonts
import org.ice1000.devkt.defaultFontName
import org.ice1000.devkt.ui.swing.AbstractUI
import org.ice1000.devkt.ui.swing.DevKtFrame
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.TitledBorder

class ConfigurationImpl(private val uiImpl: AbstractUI, parent: DevKtFrame? = null) : JDialog(parent) {
	private val mainPanel = JPanel()
	private val buttonOK = JButton()
	private val buttonCancel = JButton()
	private val buttonReset = JButton()
	private val buttonApply = JButton()
	private val backgroundImageField = JTextField()
	private val backgroundImageAlphaSlider = JSlider()
	private val editorFontField: JComboBox<String> = JComboBox()
	private val uiFontField: JComboBox<String> = JComboBox()
	private val fontSizeSpinner = JSpinner()
	private val backgroundBrowse = JButton()
	private val useLexer = JCheckBox()
	private val useParser = JCheckBox()
	private val windowIconField = JTextField()
	private val browseWindowIcon = JButton()
	private val classNameField = JTextField()
	private val jarNameField = JTextField()

	init {
		mainPanel.layout = GridLayoutManager(2, 5, Insets(10, 10, 10, 10), -1, -1)
		mainPanel.border = BorderFactory.createTitledBorder(null, "DevKt Configuration", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION)
		val spacer1 = Spacer()
		mainPanel.add(spacer1, GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		val scrollPane1 = JScrollPane()
		mainPanel.add(scrollPane1, GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false))
		val panel1 = JPanel()
		panel1.layout = GridLayoutManager(12, 3, Insets(0, 0, 0, 0), -1, -1)
		scrollPane1.setViewportView(panel1)
		val label1 = JLabel()
		label1.text = "Background image:"
		label1.setDisplayedMnemonic('B')
		label1.displayedMnemonicIndex = 0
		panel1.add(label1, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(backgroundImageField, GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		val label2 = JLabel()
		label2.text = "Editor font:"
		label2.setDisplayedMnemonic('E')
		label2.displayedMnemonicIndex = 0
		panel1.add(label2, GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val label3 = JLabel()
		label3.text = "UI font:"
		label3.setDisplayedMnemonic('U')
		label3.displayedMnemonicIndex = 0
		panel1.add(label3, GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(uiFontField, GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		val label4 = JLabel()
		label4.text = "Font size:"
		label4.setDisplayedMnemonic('S')
		label4.displayedMnemonicIndex = 5
		panel1.add(label4, GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(fontSizeSpinner, GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		backgroundBrowse.text = "Browse"
		panel1.add(backgroundBrowse, GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(editorFontField, GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		backgroundImageAlphaSlider.inverted = false
		backgroundImageAlphaSlider.majorTickSpacing = 64
		backgroundImageAlphaSlider.maximum = 255
		backgroundImageAlphaSlider.minorTickSpacing = 4
		backgroundImageAlphaSlider.paintLabels = true
		backgroundImageAlphaSlider.valueIsAdjusting = true
		panel1.add(backgroundImageAlphaSlider, GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer2 = Spacer()
		panel1.add(spacer2, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		val label5 = JLabel()
		label5.text = "Background alpha:"
		panel1.add(label5, GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val panel2 = JPanel()
		panel2.layout = GridLayoutManager(1, 2, Insets(0, 0, 0, 0), -1, -1)
		panel1.add(panel2, GridConstraints(10, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
		useLexer.text = "Use Lexer-based highight"
		useLexer.setMnemonic('L')
		useLexer.displayedMnemonicIndex = 4
		panel2.add(useLexer, GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		useParser.text = "Use Parser-based highight"
		useParser.setMnemonic('P')
		useParser.displayedMnemonicIndex = 4
		panel2.add(useParser, GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer3 = Spacer()
		panel1.add(spacer3, GridConstraints(11, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false))
		val label6 = JLabel()
		label6.text = "Window icon:"
		label6.setDisplayedMnemonic('W')
		label6.displayedMnemonicIndex = 0
		panel1.add(label6, GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(windowIconField, GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		browseWindowIcon.text = "Browse"
		panel1.add(browseWindowIcon, GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		val spacer4 = Spacer()
		panel1.add(spacer4, GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false))
		val label7 = JLabel()
		label7.text = "Build class name:"
		label7.setDisplayedMnemonic('B')
		label7.displayedMnemonicIndex = 0
		panel1.add(label7, GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(classNameField, GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		val label8 = JLabel()
		label8.text = "Build jar name:"
		label8.setDisplayedMnemonic('B')
		label8.displayedMnemonicIndex = 0
		panel1.add(label8, GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		panel1.add(jarNameField, GridConstraints(9, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, Dimension(150, -1), null, 0, false))
		buttonOK.text = "OK"
		mainPanel.add(buttonOK, GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		buttonReset.text = "Reset"
		buttonReset.setMnemonic('R')
		buttonReset.displayedMnemonicIndex = 0
		mainPanel.add(buttonReset, GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		buttonApply.text = "Apply"
		buttonApply.setMnemonic('A')
		buttonApply.displayedMnemonicIndex = 0
		mainPanel.add(buttonApply, GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		buttonCancel.text = "Cancel"
		buttonCancel.setMnemonic('C')
		buttonCancel.displayedMnemonicIndex = 0
		mainPanel.add(buttonCancel, GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false))
		label4.labelFor = fontSizeSpinner
		label6.labelFor = windowIconField
		label7.labelFor = classNameField
		label8.labelFor = jarNameField

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
		classNameField.text = javaClassName
		jarNameField.text = jarName
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
		javaClassName = classNameField.text
		jarName = jarNameField.text
		(fontSizeSpinner.value as? Number)?.let { GlobalSettings.fontSize = it.toFloat() }
		(backgroundImageAlphaSlider.value as? Number)?.let { GlobalSettings.backgroundAlpha = it.toInt() }
		monoFontName.apply {
			(parent as? DevKtFrame)?.let {
				it.ui.editorFont = Font(this, Font.PLAIN, GlobalSettings.fontSize.toInt())
			}
		}
		highlightTokenBased = useLexer.isSelected
		highlightSemanticBased = useParser.isSelected
		backgroundImage = imagePairOf(backgroundImageField)
		windowIcon = imagePairOf(windowIconField)
		uiImpl.restart()
	}

	private fun imagePairOf(windowIconField: JTextField) = windowIconField.text.let {
		try {
			it to ImageIO.read(File(it))
		} catch (e: Exception) {
			if (!it.isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"Image is invalid!",
						"Information",
						JOptionPane.ERROR_MESSAGE)
			}
			it to null
		}
	}
}
