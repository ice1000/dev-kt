package org.ice1000.devkt.ui

import charlie.gensokyo.show
import org.ice1000.devkt.config.ConfigurationImpl
import org.junit.Assert

fun main(args: Array<String>) {
	val dialog = ConfigurationImpl(uiImpl = this)
	dialog.show
	Assert.assertFalse(dialog.isVisible)
	System.exit(0)
}
