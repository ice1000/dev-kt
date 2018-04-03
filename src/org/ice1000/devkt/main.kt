@file:JvmName("Main")

package org.ice1000.devkt

import org.ice1000.devkt.lie.macCapable

/**
 * @author ice1000
 */
@JvmName("main")
fun devKt(vararg args: String) {
	`{-# LANGUAGE DarculaLookAndFeel #-}`
	`{-# LANGUAGE SarasaGothicFont #-}`
	if (macCapable) `{-# LANGUAGE MacSpecific #-}`
	`{-# LANGUAGE DevKt #-}`
}
