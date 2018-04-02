@file:Suppress("ClassName")

package org.ice1000.devkt

import com.bulenkov.darcula.DarculaLaf
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*

object `{-# LANGUAGE SarasaGothicFont #-}` {
	private var monoFont: Font?
		get() = UIManager.getFont("TextPane.font")
		set(value) {
			UIManager.put("TextPane.font", value)
		}

	private var gothicFont: Font?
		get() = UIManager.getFont("Menu.font")
		set(value) {
			UIManager.put("Menu.font", value)
			UIManager.put("MenuBar.font", value)
			UIManager.put("MenuItem.font", value)
			UIManager.put("Label.font", value)
			UIManager.put("Button.font", value)
			UIManager.put("Panel.font", value)
			UIManager.put("ToolTip.font", value)
		}

	init {
		val monoFontInputStream = javaClass.getResourceAsStream("/font/sarasa-mono-sc-regular.ttf")
		if (null != monoFontInputStream)
			monoFont = Font
					.createFont(Font.TRUETYPE_FONT, monoFontInputStream)
					.deriveFont(16F)
		val gothicFontInputStream = javaClass.getResourceAsStream("/font/sarasa-gothic-sc-regular.ttf")
		if (null != gothicFontInputStream)
			gothicFont = Font
					.createFont(Font.TRUETYPE_FONT, gothicFontInputStream)
					.deriveFont(16F)
	}
}

object `{-# LANGUAGE DarculaLookAndFeel #-}` {
	init {
		UIManager.getFont("Label.font")
		UIManager.setLookAndFeel(DarculaLaf())
	}
}

object `{-# LANGUAGE DevKt #-}` : JFrame("Dev Kt") {
	init {
		layout = BorderLayout()
		setLocation(100, 100)
		val ui = UIImpl(this)
		iconImage = Icons.iconToImage(Icons.KOTLIN_BIG)
		add(ui.mainPanel)
		pack()
		defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
		isVisible = true
	}
}
