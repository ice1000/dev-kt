package org.ice1000.devkt.lie

import com.apple.eawt.*
import com.apple.eawt.Application.getApplication
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.`{-# LANGUAGE DevKt #-}`.ui
import javax.imageio.ImageIO

inline fun mac(block: () -> Unit) {
	if (SystemInfo.isMac) block()
}

object MacSpecific : AboutHandler, PreferencesHandler, QuitHandler {
	init {
		// if System is Mac, make sure set this property before setLookAndFeel
		System.getProperties()["apple.laf.useScreenMenuBar"] = "true"
	}

	private val app: Application = getApplication()
	// TODO replace with my own icon
	val icon = ImageIO.read(javaClass.getResourceAsStream("/icon/kotlin24@2x.png"))

	init {
		app.setPreferencesHandler(this)
		app.setQuitHandler(this)
		app.setAboutHandler(this)
		app.dockIconImage = icon
	}

	override fun handlePreferences(event: AppEvent.PreferencesEvent) = ui.settings()
	override fun handleAbout(event: AppEvent.AboutEvent) = Unit
	override fun handleQuitRequestWith(event: AppEvent.QuitEvent, quitResponse: QuitResponse) = ui.exit()
}