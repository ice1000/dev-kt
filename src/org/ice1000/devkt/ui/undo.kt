package org.ice1000.devkt.ui

import org.jetbrains.kotlin.com.intellij.util.containers.Stack

class Edit(val offset: Int, val text: String, val isInsert: Boolean) {
	fun invert() = Edit(offset, text, !isInsert)
	val length get() = text.length
}

/**
 * @author ice1000
 * @since v1.3
 * @property undoStack Stack<Edit?> Use null as mark
 * @property redoStack Stack<Edit?> Use null as mark
 * @property canUndo Boolean if there's undo available
 * @property canRedo Boolean if there's undo available
 */
class DevKtUndoManager {
	private val undoStack = Stack<Edit?>()
	private val redoStack = Stack<Edit?>()
	val canUndo get() = undoStack.isNotEmpty()
	val canRedo get() = redoStack.isNotEmpty()

	fun undo(host: DevKtDocumentHandler<*>) {
		if (!canUndo) return
		while (null == undoStack.peek()) undoStack.pop()
		generateSequence { undoStack.pop() }.forEach {
			redoStack.push(it)
			if (it.isInsert) host.deleteDirectly(it.offset, it.length, reparse = false)
			else host.insertDirectly(it.offset, it.text, reparse = false)
		}
		host.reparse()
		redoStack.push(null)
	}

	fun redo(host: DevKtDocumentHandler<*>) {
		if (!canRedo) return
		while (null == redoStack.peek()) redoStack.pop()
		generateSequence { redoStack.pop() }.forEach {
			undoStack.push(it)
			if (it.isInsert) host.insertDirectly(it.offset, it.text, reparse = false)
			else host.deleteDirectly(it.offset, it.length, reparse = false)
		}
		host.reparse()
		undoStack.push(null)
	}

	fun addEdit(edit: Edit) {
		undoStack.add(edit)
		redoStack.clear()
	}

	fun done() {
		if (undoStack.empty() || null != undoStack.peek())
			undoStack.push(null)
	}
}
