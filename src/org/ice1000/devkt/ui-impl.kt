package org.ice1000.devkt

import charlie.gensokyo.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.undo.UndoManager

fun JFrame.unfinished() {
	JOptionPane.showMessageDialog(this, "This feature is unfinished.",
			"Unfinished", 1, Icons.KOTLIN)
}

/**
 * @author ice1000
 */
class UIImpl(private val frame: JFrame) : UI() {
	private val undoManager = UndoManager()
	private lateinit var undoMenuItem: JMenuItem
	private lateinit var redoMenuItem: JMenuItem

	init {
		frame.jMenuBar = menuBar
		editor.document.addUndoableEditListener {
			undoManager.addEdit(it.edit)
			updateUndoMenuItems()
		}
		menuBar.subMenu("File") {
			mnemonic = KeyEvent.VK_F
			item("Open") {
				onAction { frame.unfinished() }
			}
			item("Save") {
				onAction { frame.unfinished() }
			}
			item("Exit") {
				onAction {
					frame.dispose()
					// TODO check saving
					System.exit(0)
				}
			}
		}
		menuBar.subMenu("Edit") {
			mnemonic = KeyEvent.VK_E
			item("Undo") {
				undoMenuItem = this
				onAction { undoManager.undo() }
			}
			item("Redo") {
				redoMenuItem = this
				onAction { undoManager.redo() }
			}
		}
		updateUndoMenuItems()
		editor.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				if (e.isControlDown && !e.isAltDown && !e.isShiftDown && e.keyCode == KeyEvent.VK_Z) {
					if (undoManager.canUndo()) undoManager.undo()
				}
				if (e.isControlDown && !e.isAltDown && e.isShiftDown && e.keyCode == KeyEvent.VK_Z) {
					if (undoManager.canRedo()) undoManager.redo()
				}
			}
		})
	}

	private fun updateUndoMenuItems() {
		undoMenuItem.isEnabled = undoManager.canUndo()
		redoMenuItem.isEnabled = undoManager.canRedo()
	}
}
