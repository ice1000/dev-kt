package org.ice1000.devkt.ui.swing

import org.ice1000.devkt.config.ShortCut
import java.awt.event.KeyEvent
import javax.swing.JMenuItem
import javax.swing.KeyStroke

/**
 * <kbd>Ctrl</kbd> for Windows/Linux, <kbd>Meta</kbd> for MacOS
 * Replacement of [java.awt.Toolkit.getMenuShortcutKeyMask]
 * @param key like [KeyEvent.VK_S]
 */
fun JMenuItem.keyMap(key: Int, modifiers: Int) {
	if (key != 0 && modifiers != 0)
		accelerator = KeyStroke.getKeyStroke(key, modifiers)
}

fun JMenuItem.keyMap(shortcut: ShortCut) = keyMap(shortcut.keyCode, shortcut.modifier)
