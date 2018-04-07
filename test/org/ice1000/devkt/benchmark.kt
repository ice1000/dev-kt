package org.ice1000.devkt

/**
 * @author ice1000
 */
@JvmName("main")
fun benchmark(vararg args: String) {
	val init = System.currentTimeMillis()
	`{-# LANGUAGE MacSpecific #-}`
	val time = System.currentTimeMillis()
	`{-# LANGUAGE DarculaLookAndFeel #-}`
	val time2 = System.currentTimeMillis()
	`{-# LANGUAGE SarasaGothicFont #-}`
	val time3 = System.currentTimeMillis()
	`{-# LANGUAGE DevKt #-}`
	println("${time - init}, ${time2 - time}, ${time3 - time2}, ${System.currentTimeMillis() - time3}")
}
