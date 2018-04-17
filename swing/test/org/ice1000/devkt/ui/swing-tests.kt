package org.ice1000.devkt.ui

import charlie.gensokyo.exitOnClose
import charlie.gensokyo.show
import charlie.gensokyo.size
import javax.swing.JFrame

fun main(args: Array<String>) {
	JFrame().apply {
		size(100, 100)
		exitOnClose
	}.show
}
