@file:JvmName("Main")

package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lie.mac

/**
 * @author ice1000
 */
@JvmName("main")
fun devKt(vararg args: String) {
	GlobalSettings.load()
	if (mac) `{-# LANGUAGE MacSpecific #-}`
	`{-# LANGUAGE DarculaLookAndFeel #-}`
	`{-# LANGUAGE SarasaGothicFont #-}`
	`{-# LANGUAGE DevKt #-}`
}
