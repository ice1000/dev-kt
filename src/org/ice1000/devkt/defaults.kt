@file:Suppress("ClassName", "ObjectPropertyName")
@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import charlie.gensokyo.doNothingOnClose
import charlie.gensokyo.show
import com.bulenkov.darcula.DarculaLaf
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.lie.mac
import org.ice1000.devkt.ui.UIImpl
import java.awt.Font
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.UIManager

object `{-# LANGUAGE SarasaGothicFont #-}` {
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
			UIManager.put("TextField.font", value)
			UIManager.put("Button.font", value)
			UIManager.put("Panel.font", value)
			UIManager.put("ToolTip.font", value)
		}

	init {
		loadFont()
	}

	fun loadFont() {
		val mono = GlobalSettings.monoFontName.trim()
		if (mono.isEmpty() or
				mono.equals("DevKt Default", true)) {
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
				gothic.equals("DevKt Default", true)) {
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

val `{-# LANGUAGE DarculaLookAndFeel #-}`: Unit
	get() {
		UIManager.getFont("Label.font")
		UIManager.setLookAndFeel(DarculaLaf())
	}

object `{-# LANGUAGE DevKt #-}` : JFrame() {
	val ui = UIImpl(this)

	init {
		GlobalSettings.windowIcon.second.also {
			iconImage = it
			if (mac) MacSpecific.app.dockIconImage = it
		}
		add(ui.mainPanel)
		addWindowListener(object : WindowAdapter() {
			override fun windowDeactivated(e: WindowEvent?) = GlobalSettings.save()
			override fun windowLostFocus(e: WindowEvent?) = GlobalSettings.save()
			override fun windowClosing(e: WindowEvent?) {
				GlobalSettings.save()
				ui.exit()
			}
		})
		addComponentListener(object : ComponentAdapter() {
			override fun componentMoved(e: ComponentEvent?) {
				GlobalSettings.windowBounds = bounds
			}

			override fun componentResized(e: ComponentEvent?) {
				super.componentResized(e)
				GlobalSettings.windowBounds = bounds
				ui.imageCache = null
			}
		})
		bounds = GlobalSettings.windowBounds
		doNothingOnClose
		show
		with(ui) {
			postInit()
			refreshTitle()
		}
	}
}

val `{-# LANGUAGE MacSpecific #-}`: Unit
	get() {
		if (mac) MacSpecific
	}
