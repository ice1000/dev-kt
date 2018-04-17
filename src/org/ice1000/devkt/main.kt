@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.lie.mac
import org.ice1000.devkt.ui.swing.DevKtFrame

/**
 * @author ice1000
 */
@JvmName("main")
fun devKt(vararg args: String) {
	with(LaunchInfo) {
		load(*args)
		if (redirectStdout) redirectStdout()
		GlobalSettings.load()
		if (!ugly) {
			if (mac) MacSpecific.init()
			useDarculaLaf()
		}
		if (!noFont) DevKtFontManager.loadFont()
		DevKtFrame()
	}
}
