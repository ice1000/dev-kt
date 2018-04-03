package org.ice1000.devkt.lie

import com.apple.eawt.*
import com.apple.eawt.Application.getApplication
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.`{-# LANGUAGE DevKt #-}`
import org.ice1000.devkt.ui.AllIcons.KOTLIN_BIG_ICON
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Transparency
import javax.swing.Icon
import javax.swing.ImageIcon

inline fun mac(block: () -> Unit) {
	if (SystemInfo.isMac) block()
}

object MacSpecific : AboutHandler, PreferencesHandler, QuitHandler {
	init {
		// if System is Mac, make sure set this property before setLookAndFeel
		System.getProperties()["apple.laf.useScreenMenuBar"] = "true"
	}

	private val app: Application = getApplication()

	init {
		app.setPreferencesHandler(this)
		app.setQuitHandler(this)
		app.setAboutHandler(this)
		app.dockIconImage = KOTLIN_BIG_ICON.let(::toImage)
	}

	override fun handlePreferences(event: AppEvent.PreferencesEvent) =
			`{-# LANGUAGE DevKt #-}`.ui.settings()

	override fun handleAbout(event: AppEvent.AboutEvent) = Unit

	override fun handleQuitRequestWith(event: AppEvent.QuitEvent, quitResponse: QuitResponse) =
			`{-# LANGUAGE DevKt #-}`.ui.exit()


	/**
	 * we could only implements it to convert a Icon to a Image
	 * Don't ask why we didn't use the API.
	 * I'm too stupid.
	 *
	 * @param icon built-in icons [org.ice1000.devkt.ui.AllIcons]
	 */
	private fun toImage(icon: Icon): Image {
		return if (icon is ImageIcon) {
			icon.image
		} else {
			GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.defaultScreenDevice
					.defaultConfiguration
					.createCompatibleImage(icon.iconWidth, icon.iconHeight, Transparency.TRANSLUCENT).apply img@{
						this@img.createGraphics().apply {
							icon.paintIcon(null, this, 0, 0)
							dispose()
						}
					}
		}
	}
}