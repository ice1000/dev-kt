@file:Suppress("ClassName", "ObjectPropertyName")
@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import com.bulenkov.darcula.DarculaLaf
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.ui.swing.DevKtFrame
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.UIManager

fun redirectStdout() = System.setOut(PrintStream(object : OutputStream() {
	override fun write(b: Int) {
		if (b.toChar() == '\n') DevKtFrame.instance.ui.messageLabel.text = ""
		else DevKtFrame.instance.ui.messageLabel.run { text = "$text${b.toChar()}" }
	}
}))

object DevKtFontManager {
	var monoFont: Font
		get() = UIManager.getFont("TextPane.font")
		set(value) {
			UIManager.put("TextPane.font", value)
			UIManager.put("List.font", value)
		}

	var gothicFont: Font
		get() = UIManager.getFont("Panel.font")
		set(value) {
			UIManager.put("Menu.font", value)
			UIManager.put("MenuBar.font", value)
			UIManager.put("MenuItem.font", value)
			UIManager.put("Label.font", value)
			UIManager.put("Spinner.font", value)
			UIManager.put("MenuItem.acceleratorFont", value)
			UIManager.put("FormattedTextField.font", value)
			UIManager.put("TextField.font", value)
			UIManager.put("FileChooser.font", value)
			UIManager.put("Button.font", value)
			UIManager.put("Table.font", value)
			UIManager.put("Panel.font", value)
			UIManager.put("CheckBox.font", value)
			UIManager.put("ComboBox.font", value)
			UIManager.put("ToolTip.font", value)
		}

	val allFonts: Array<String> by lazy {
		GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
	}

	fun loadFont() {
		val mono = GlobalSettings.monoFontName.trim()
		if (mono.isEmpty() or
				mono.equals(defaultFontName, true)) {
			val monoFontInputStream = javaClass.getResourceAsStream("/font/sarasa-mono-sc-regular.ttf")
					?: javaClass.getResourceAsStream("/font/FiraCode-Regular.ttf")
			if (null != monoFontInputStream)
				monoFont = Font
						.createFont(Font.TRUETYPE_FONT, monoFontInputStream)
						.deriveFont(GlobalSettings.fontSize)
		} else {
			monoFont = Font(mono, Font.TRUETYPE_FONT, 16).deriveFont(GlobalSettings.fontSize)
		}
		val gothic = GlobalSettings.gothicFontName.trim()
		if (gothic.isEmpty() or
				gothic.equals(defaultFontName, true)) {
			val gothicFontInputStream = javaClass.getResourceAsStream("/font/sarasa-gothic-sc-regular.ttf")
			if (null != gothicFontInputStream)
				gothicFont = Font
						.createFont(Font.TRUETYPE_FONT, gothicFontInputStream)
						.deriveFont(GlobalSettings.fontSize)
		} else {
			gothicFont = Font(gothic, Font.TRUETYPE_FONT, 16).deriveFont(GlobalSettings.fontSize)
		}
	}
}

fun useDarculaLaf() {
	UIManager.getFont("Label.font")
	UIManager.setLookAndFeel(DarculaLaf())
}
