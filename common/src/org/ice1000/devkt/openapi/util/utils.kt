package org.ice1000.devkt.openapi.util

import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.GlobalSettings
import java.io.*
import javax.swing.JOptionPane

data class Quad<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

inline fun ignoreException(lambda: () -> Unit) {
	try {
		lambda()
	} catch (ignored: Exception) {
	}
}

inline fun handleException(lambda: () -> Unit) {
	try {
		lambda()
	} catch (e: Throwable) {
		val text = StringBuilder()
		e.printStackTrace(PrintStream(object : OutputStream() {
			override fun write(byte: Int) {
				text.append(byte.toChar())
			}
		}))
		JOptionPane.showMessageDialog(
				null,
				text,
				"Failed to load plugin",
				JOptionPane.ERROR_MESSAGE
		)
	}
}

val selfLocation: String = Analyzer::class.java.protectionDomain.codeSource.location.file
val selfLocationFile: File = File(selfLocation)

val insteadPaired: Map<Char, Lazy<String>> = mapOf(
		'\t' to lazy { if (GlobalSettings.useTab) "\t" else " ".repeat(GlobalSettings.tabSize) }
)

val paired = mapOf(
		'"' to '"',
		'\'' to '\'',
		'“' to '”',
		'‘' to '’',
		'`' to '`',
		'(' to ')',
		'（' to '）',
		'『' to '』',
		'「' to '」',
		'〖' to '〗',
		'【' to '】',
		'[' to ']',
		'〔' to '〕',
		'［' to '］',
		'{' to '}',
		'｛' to '｝',
		'<' to '>',
		'《' to '》',
		'〈' to '〉',
		'‹' to '›',
		'«' to '»'
)

fun cutText(string: String, max: Int) =
		if (string.length <= max) string else "${string.take(max)}…"

fun isHex(c: Char): Boolean = isHex(c.toByte())

fun isHex(c: Byte): Boolean {
	return c in 48..57 || c in 97..102 || c in 65..70
}
