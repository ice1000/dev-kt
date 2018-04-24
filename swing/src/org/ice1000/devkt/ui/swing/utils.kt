package org.ice1000.devkt.ui.swing

import org.ice1000.devkt.config.Key
import org.ice1000.devkt.config.ShortCut
import org.ice1000.devkt.openapi.util.CompletionPopup
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * <kbd>Ctrl</kbd> for Windows/Linux, <kbd>Meta</kbd> for MacOS
 * Replacement of [java.awt.Toolkit.getMenuShortcutKeyMask]
 * @param key like [KeyEvent.VK_S]
 */
fun JMenuItem.keyMap(key: Int, modifiers: Int) {
	if (key != 0 && modifiers != 0)
		accelerator = KeyStroke.getKeyStroke(key, modifiers)
}

fun JMenuItem.keyMap(shortcut: ShortCut) = keyMap(shortcut.key.awt, shortcut.modifier)

class SwingPopup(private val popup: Popup) : CompletionPopup {
	override fun show() = popup.show()
	override fun hide() = popup.hide()
}

/**
 * @author ice1000
 * @since v1.3
 */
val Key.awt
	get() = when (this) {
		Key.A -> KeyEvent.VK_A
		Key.B -> KeyEvent.VK_B
		Key.C -> KeyEvent.VK_C
		Key.D -> KeyEvent.VK_D
		Key.E -> KeyEvent.VK_E
		Key.F -> KeyEvent.VK_F
		Key.G -> KeyEvent.VK_G
		Key.H -> KeyEvent.VK_H
		Key.I -> KeyEvent.VK_I
		Key.J -> KeyEvent.VK_J
		Key.K -> KeyEvent.VK_K
		Key.L -> KeyEvent.VK_L
		Key.M -> KeyEvent.VK_M
		Key.N -> KeyEvent.VK_N
		Key.O -> KeyEvent.VK_O
		Key.P -> KeyEvent.VK_P
		Key.Q -> KeyEvent.VK_Q
		Key.R -> KeyEvent.VK_R
		Key.S -> KeyEvent.VK_S
		Key.T -> KeyEvent.VK_T
		Key.U -> KeyEvent.VK_U
		Key.V -> KeyEvent.VK_V
		Key.W -> KeyEvent.VK_W
		Key.X -> KeyEvent.VK_X
		Key.Y -> KeyEvent.VK_Y
		Key.Z -> KeyEvent.VK_Z
		Key.`0` -> KeyEvent.VK_0
		Key.`1` -> KeyEvent.VK_1
		Key.`2` -> KeyEvent.VK_2
		Key.`3` -> KeyEvent.VK_3
		Key.`4` -> KeyEvent.VK_4
		Key.`5` -> KeyEvent.VK_5
		Key.`6` -> KeyEvent.VK_6
		Key.`7` -> KeyEvent.VK_7
		Key.`8` -> KeyEvent.VK_8
		Key.`9` -> KeyEvent.VK_9
		Key.F1 -> KeyEvent.VK_F1
		Key.F2 -> KeyEvent.VK_F2
		Key.F3 -> KeyEvent.VK_F3
		Key.F4 -> KeyEvent.VK_F4
		Key.F5 -> KeyEvent.VK_F5
		Key.F6 -> KeyEvent.VK_F6
		Key.F7 -> KeyEvent.VK_F7
		Key.F8 -> KeyEvent.VK_F8
		Key.F9 -> KeyEvent.VK_F9
		Key.F10 -> KeyEvent.VK_F10
		Key.F11 -> KeyEvent.VK_F11
		Key.F12 -> KeyEvent.VK_F12
		Key.SLASH -> KeyEvent.VK_SLASH
		Key.ENTER -> KeyEvent.VK_ENTER
		Key.UP -> KeyEvent.VK_UP
		Key.DOWN -> KeyEvent.VK_DOWN
		Key.LEFT -> KeyEvent.VK_LEFT
		Key.RIGHT -> KeyEvent.VK_RIGHT
	}
