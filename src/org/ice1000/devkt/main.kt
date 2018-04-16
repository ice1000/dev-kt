@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import javafx.application.Application
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.lie.mac
import org.ice1000.devkt.ui.jfx.DevKtApplication

/**
 * @author ice1000
 */
@JvmName("main")
fun devKt(vararg args: String) {
	// redirectStdout()
	GlobalSettings.load()
	if (args.firstOrNull() != "--ugly") {
		if (mac) MacSpecific.init()
	}
	DevKtFontManager.loadFont()
	Application.launch(DevKtApplication::class.java, *args)
}
