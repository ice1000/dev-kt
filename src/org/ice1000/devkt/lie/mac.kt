package org.ice1000.devkt.lie

import com.apple.eawt.*
import com.apple.eawt.Application.getApplication
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.`{-# LANGUAGE DarculaLookAndFeel #-}`
import org.ice1000.devkt.`{-# LANGUAGE DevKt #-}`.ui

val mac = SystemInfo.isMac

object MacSpecific : AboutHandler, PreferencesHandler, QuitHandler {
	init {
		// if System is Mac, make sure set this property before setLookAndFeel
		System.getProperties()["apple.laf.useScreenMenuBar"] = "true"
	}

	val app: Application = getApplication()

	init {
		app.setPreferencesHandler(this)
		app.setQuitHandler(this)
		app.setAboutHandler(this)
		// TODO replace with my own icon
	}

	override fun handlePreferences(event: AppEvent.PreferencesEvent) = ui.settings()
	override fun handleAbout(event: AppEvent.AboutEvent) = Unit
	override fun handleQuitRequestWith(event: AppEvent.QuitEvent, quitResponse: QuitResponse) = ui.exit()
}