package org.ice1000.devkt

import org.ice1000.devkt.lie.MacSpecific
import org.ice1000.devkt.lie.mac
import org.ice1000.devkt.ui.swing.DevKtFrame

/**
 * @author ice1000
 */
@JvmName("main")
fun benchmark(vararg args: String) {
	val init = System.currentTimeMillis()
	if (mac) MacSpecific
	val time = System.currentTimeMillis()
	get { -# LANGUAGE DarculaLookAndFeel #- }()
	val time2 = System.currentTimeMillis()
	DevKtFontManager.loadFont()
	val time3 = System.currentTimeMillis()
	DevKtFrame()
	println("${time - init}, ${time2 - time}, ${time3 - time2}, ${System.currentTimeMillis() - time3}")
}
