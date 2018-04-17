@file:Suppress("ClassName", "ObjectPropertyName")
@file:JvmName("Main")
@file:JvmMultifileClass

package org.ice1000.devkt

object LaunchInfo {
	fun load(vararg args: String) = args.forEach { arg ->
		when (arg) {
			"--ugly" -> ugly = true
			"--no-font" -> noFont = true
			"--no-bg" -> noBg = true
			"--redirect-stdout" -> redirectStdout = true
		}
	}

	var ugly = false
	var noFont = false
	var noBg = false
	var redirectStdout = false
}

const val defaultFontName = "DevKt Default"
