package org.ice1000.devkt.lie

import com.apple.eawt.*
import com.apple.eawt.Application.getApplication
import com.bulenkov.iconloader.util.SystemInfo

val macCapable = SystemInfo.isMac and SystemInfo.isAppleJvm

@Suppress("LeakingThis")
abstract class MacSpecific : AboutHandler, PreferencesHandler, QuitHandler {
	private val app: Application = getApplication()

	init {
		app.setPreferencesHandler(this)
		app.setQuitHandler(this)
		app.setAboutHandler(this)
	}

	override fun handlePreferences(event: AppEvent.PreferencesEvent) = Unit
	override fun handleAbout(event: AppEvent.AboutEvent) = Unit
	override fun handleQuitRequestWith(event: AppEvent.QuitEvent, quitResponse: QuitResponse) {
		// `{-# LANGUAGE DevKt #-}`.ui.makeSureLeaveCurrentFile()
	}
}
