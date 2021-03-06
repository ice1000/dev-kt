package org.ice1000.devkt

import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.ui.swing.DevKtFrame
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo

/**
 * @author ice1000
 */
@JvmName("main")
fun benchmark(vararg args: String) {
	val init = System.currentTimeMillis()
	if (SystemInfo.isMac) MacSpecific
	val time = System.currentTimeMillis()
	useDarculaLaf()
	val time2 = System.currentTimeMillis()
	DevKtFontManager.loadFont()
	val time3 = System.currentTimeMillis()
	DevKtFrame()
	println("${time - init}, ${time2 - time}, ${time3 - time2}, ${System.currentTimeMillis() - time3}")
}
