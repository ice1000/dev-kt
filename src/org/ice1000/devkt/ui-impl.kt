package org.ice1000.devkt

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.undo.UndoManager

/**
 * @author ice1000
 */
class UIImpl(private val frame: JFrame) : UI() {
	private val undoManager = UndoManager()

	init {
		frame.jMenuBar = menuBar
		editor.document.addUndoableEditListener {
			undoManager.addEdit(it.edit)
			println(undoManager.canUndo())
			println(undoManager.canRedo())
		}
		editor.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				if (e.isControlDown && !e.isAltDown && !e.isShiftDown && e.keyCode == KeyEvent.VK_Z) undoManager.undo()
				if (e.isControlDown && !e.isAltDown && e.isShiftDown && e.keyCode == KeyEvent.VK_Z) undoManager.redo()
			}
		})
	}
}
