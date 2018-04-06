package org.ice1000.devkt.ui

import charlie.gensokyo.show
import org.ice1000.devkt.`{-# LANGUAGE DevKt #-}`
import org.ice1000.devkt.config.ConfigurationImpl
import org.junit.Assert

fun main(args: Array<String>) {
	val dialog = ConfigurationImpl(UIImpl(`{-# LANGUAGE DevKt #-}`))
	dialog.show
	Assert.assertFalse(dialog.isVisible)
	System.exit(0)
}
