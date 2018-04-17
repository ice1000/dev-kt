package org.ice1000.devkt.ui

import charlie.gensokyo.show
import org.ice1000.devkt.ui.swing.dialogs.ConfigurationImpl
import org.ice1000.devkt.ui.swing.DevKtFrame
import org.ice1000.devkt.ui.swing.UIImpl
import org.junit.Assert

fun main(args: Array<String>) {
	val dialog = ConfigurationImpl(UIImpl(DevKtFrame()))
	dialog.show
	Assert.assertFalse(dialog.isVisible)
	System.exit(0)
}
