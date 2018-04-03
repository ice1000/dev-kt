@file:Suppress("ClassName")

package org.ice1000.devkt

import com.bulenkov.darcula.DarculaLaf
import com.intellij.openapi.util.SystemInfo
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.lie.invoke
import org.ice1000.devkt.lie.macCapable
import org.ice1000.devkt.ui.UIImpl
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.*
import javax.imageio.ImageIO
import javax.swing.*

object `{-# LANGUAGE SarasaGothicFont #-}` {
	var monoFont: Font
		get() = UIManager.getFont("TextPane.font")
		set(value) {
			UIManager.put("TextPane.font", value)
		}

	private var gothicFont: Font
		get() = UIManager.getFont("Panel.font")
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

object `{-# LANGUAGE DevKt #-}` : JFrame() {
	const val defaultTitle = "Dev Kt"
	val globalSettings = GlobalSettings()
	val ui: UIImpl

	init {
		macCapable {
			System.getProperties()["apple.laf.useScreenMenuBar"] = "true"
		}
		layout = BorderLayout()
		title = defaultTitle
		globalSettings.load()
		setLocation(100, 100)
		ui = UIImpl(this)
		// TODO replace with my own icon
		iconImage = ImageIO.read(javaClass.getResourceAsStream("/icon/kotlin24@2x.png"))
		add(ui.mainPanel)
		pack()
		addFocusListener(object : FocusAdapter() {
			override fun focusLost(e: FocusEvent?) = globalSettings.save()
		})
		addWindowListener(object : WindowAdapter() {
			override fun windowDeactivated(e: WindowEvent?) = globalSettings.save()
			override fun windowClosing(e: WindowEvent?) = globalSettings.save()
		})
		defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
		isVisible = true
	}
}

object `{-# LANGUAGE MacSpecific #-}` : MacSpecific()
