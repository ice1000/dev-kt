package org.ice1000.devkt

import org.ice1000.devkt.config.ShortCut
import java.awt.event.KeyEvent
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.*

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

val paired = mapOf(
		'"' to '"',
		'\'' to '\'',
		'“' to '”',
		'‘' to '’',
		'`' to '`',
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

/**
 * <kbd>Ctrl</kbd> for Windows/Linux, <kbd>Meta</kbd> for MacOS
 * Replacement of [java.awt.Toolkit.getMenuShortcutKeyMask]
 * @param key like [KeyEvent.VK_S]
 */
fun JMenuItem.keyMap(key: Int, modifiers: Int) {
	accelerator = KeyStroke.getKeyStroke(key, modifiers)
}

fun JMenuItem.keyMap(shortcut: ShortCut) = keyMap(shortcut.keyCode, shortcut.modifier)

fun CharSequence.subString(from: Int, len: Int) = substring(from, from + len)
