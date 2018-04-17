package org.ice1000.devkt.lie

import com.apple.eawt.*
import com.apple.eawt.Application.getApplication
import org.ice1000.devkt.config.GlobalSettings
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import java.awt.event.KeyEvent

val ctrlOrMeta = if (SystemInfo.isMac) KeyEvent.META_DOWN_MASK else KeyEvent.CTRL_DOWN_MASK

object MacSpecific : AboutHandler, PreferencesHandler, QuitHandler {
	init {
		// if System is Mac, make sure set this property before setLookAndFeel
		System.getProperties()["apple.laf.useScreenMenuBar"] = "true"
	}

	fun init() {
		val app: Application = getApplication()
		app.setPreferencesHandler(this)
		app.setQuitHandler(this)
		app.setAboutHandler(this)
		app.dockIconImage = GlobalSettings.windowIcon.second
		// TODO replace with my own icon
	}

	override fun handlePreferences(event: AppEvent.PreferencesEvent) = TODO()
	override fun handleAbout(event: AppEvent.AboutEvent) = Unit
	override fun handleQuitRequestWith(event: AppEvent.QuitEvent, quitResponse: QuitResponse) = TODO()
}
