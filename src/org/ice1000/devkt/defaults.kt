@file:Suppress("ClassName")

package org.ice1000.devkt

import charlie.gensokyo.doNothingOnClose
import com.bulenkov.darcula.DarculaLaf
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.ui.UIImpl
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
		?: javaClass.getResourceAsStream("/font/FiraCode-Regular.ttf")
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
	val icon = ImageIO.read(javaClass.getResourceAsStream("/icon/kotlin24@2x.png"))

	init {
		UIManager.getFont("Label.font")
		UIManager.setLookAndFeel(DarculaLaf())
	}
}

object `{-# LANGUAGE DevKt #-}` : JFrame() {
	val globalSettings = GlobalSettings()
	val ui: UIImpl

	init {
		globalSettings.load()
		ui = UIImpl(this)
		iconImage = `{-# LANGUAGE DarculaLookAndFeel #-}`.icon
		add(ui.mainPanel)
		addWindowListener(object : WindowAdapter() {
			override fun windowDeactivated(e: WindowEvent?) = globalSettings.save()
			override fun windowLostFocus(e: WindowEvent?) = globalSettings.save()
			override fun windowClosing(e: WindowEvent?) {
				globalSettings.save()
				ui.exit()
			}
		})
		addComponentListener(object : ComponentAdapter() {
			override fun componentMoved(e: ComponentEvent?) {
				globalSettings.windowBounds = bounds
			}

			override fun componentResized(e: ComponentEvent?) {
				super.componentResized(e)
				globalSettings.windowBounds = bounds
			}
		})
		doNothingOnClose
		bounds = globalSettings.windowBounds
		isVisible = true
		with(ui) {
			postInit()
			refreshTitle()
		}
	}
}

typealias `{-# LANGUAGE MacSpecific #-}` = MacSpecific
