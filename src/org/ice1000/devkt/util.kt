package org.ice1000.devkt

import com.apple.eawt.*
import org.ice1000.devkt.ui.TODO


/**
 * @author zxj5470
 * @date 2018/4/3
 */
object SystemInfo {
	private val name: String
		get() = System.getProperty("os.name")

	val isMac: Boolean
		get() = name.startsWith("Mac")
}

object system {
	operator fun set(key: String, value: Any) {
		System.setProperty(key, value.toString())
	}
}

class MacApplicationListner(private val devKt: `{-# LANGUAGE DevKt #-}`) : AboutHandler, PreferencesHandler, QuitHandler {
	private val app: Application = com.apple.eawt.Application.getApplication()

	init {
		app.setPreferencesHandler(this)
		app.setQuitHandler(this)
		app.setAboutHandler(this)
	}

	override fun handleQuitRequestWith(event: AppEvent.QuitEvent, quitResponse: QuitResponse) {
		// if file unsaved...
		System.exit(0)
	}

	override fun handlePreferences(event: AppEvent.PreferencesEvent) {
		// settings
		devKt.TODO()
	}

	override fun handleAbout(event: AppEvent.AboutEvent) {
		// about
		devKt.TODO()
	}
}