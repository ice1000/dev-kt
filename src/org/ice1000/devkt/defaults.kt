@file:Suppress("ClassName", "ObjectPropertyName")
@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.UIManager

object DevKtFontManager {
	var monoFont: Font
		get() = UIManager.getFont("TextPane.font")
		set(value) {
			UIManager.put("TextPane.font", value)
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
			UIManager.put("List.font", value)
			UIManager.put("Table.font", value)
			UIManager.put("Panel.font", value)
			UIManager.put("CheckBox.font", value)
			UIManager.put("ComboBox.font", value)
			UIManager.put("ToolTip.font", value)
		}

	const val defaultFontName = "DevKt Default"
	val allFonts by lazy {
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

object LaunchInfo {
	fun load(vararg args: String) = args.forEach { arg ->
		when (arg) {
			"--ugly" -> ugly = true
			"--no-font" -> noFont = true
			"--no-bg" -> noBg = true
			"--redirect-stdout" -> redirectStdout = true
		}
	}

	var ugly = false
	var noFont = false
	var noBg = false
	var redirectStdout = false
}
