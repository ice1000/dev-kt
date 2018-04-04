package org.ice1000.devkt.ui

import org.ice1000.devkt.Kotlin
import org.ice1000.devkt.psi.PsiViewerImpl
import org.junit.Assert.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@Throws(IOException::class)
fun main(args: Array<String>) {
	val dialog = PsiViewerImpl(Kotlin.parse(String(Files.readAllBytes(Paths.get(
			"src", "org", "ice1000", "devkt", "kotlin.kt")))))
	dialog.pack()
	dialog.isVisible = true
	assertTrue(dialog.isVisible)
	System.exit(0)
}
