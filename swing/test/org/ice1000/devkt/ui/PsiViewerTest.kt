package org.ice1000.devkt.ui

import charlie.gensokyo.show
import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.lang.PsiViewerImpl
import org.junit.Assert.assertFalse
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@Throws(IOException::class)
fun main(args: Array<String>) {
	val dialog = PsiViewerImpl(Analyzer.parseKotlin(String(Files.readAllBytes(Paths.get(
			"src", "org", "ice1000", "devkt", "analyze.kt")))))
	dialog.show
	assertFalse(dialog.isVisible)
	System.exit(0)
}
