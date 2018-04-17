@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.ui.swing.DevKtFrame
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo

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
			if (SystemInfo.isMac) MacSpecific.init()
			useDarculaLaf()
		}
		if (!noFont) DevKtFontManager.loadFont()
		DevKtFrame()
	}
}
