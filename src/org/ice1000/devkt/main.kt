@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings

/**
 * @author ice1000
 */
@JvmName("main")
fun devKt(vararg args: String) {
	GlobalSettings.load()
	`{-# LANGUAGE MacSpecific #-}`
	`{-# LANGUAGE DarculaLookAndFeel #-}`
	`{-# LANGUAGE SarasaGothicFont #-}`
	`{-# LANGUAGE DevKt #-}`
}
