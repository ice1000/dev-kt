package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import java.io.File

fun main(args: Array<String>) {
	val file = File("template/script.kts")
	GlobalSettings.load()
	Kotlin.compileScript(file)
}
