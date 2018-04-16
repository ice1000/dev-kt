package org.ice1000.devkt.ui

import org.jetbrains.kotlin.com.intellij.util.containers.Stack

class Edit(val offset: Int, val text: CharSequence, val isInsert: Boolean) {
	fun invert() = Edit(offset, text, !isInsert)
	val length get() = text.length
}

fun <T> Stack<T>.popOrNull() = if (empty()) null else pop()
fun <T> Stack<T>.peekOrNull() = if (empty()) null else peek()

/**
 * @author ice1000
 * @since v1.3
 * @property undoStack Stack<Edit?> Use null as mark
 * @property redoStack Stack<Edit?> Use null as mark
 * @property canUndo Boolean if there's undo available
 * @property canRedo Boolean if there's undo available
 */
class DevKtUndoManager(initialCapacity: Int) {
	constructor() : this(160)

	private val undoStack = Stack<Edit?>(initialCapacity)
	private val redoStack = Stack<Edit?>(initialCapacity)
	val canUndo get() = undoStack.isNotEmpty()
	val canRedo get() = redoStack.isNotEmpty()

	fun clear() {
		undoStack.clear()
		redoStack.clear()
	}

	fun undo(host: DevKtDocumentHandler<*>) {
		if (!canUndo) return
		while (null == undoStack.peek()) undoStack.pop()
		generateSequence { undoStack.popOrNull() }.forEach {
			redoStack.push(it)
			if (it.isInsert) host.deleteDirectly(it.offset, it.length, reparse = false)
			else host.insertDirectly(it.offset, it.text.toString(), move = 0, reparse = false)
		}
		host.reparse()
		doneUndo()
	}

	fun redo(host: DevKtDocumentHandler<*>) {
		if (!canRedo) return
		while (null == redoStack.peek()) redoStack.pop()
		generateSequence { redoStack.popOrNull() }.forEach {
			undoStack.push(it)
			if (it.isInsert) host.insertDirectly(it.offset, it.text.toString(), move = 0, reparse = false)
			else host.deleteDirectly(it.offset, it.length, reparse = false)
		}
		host.reparse()
		done()
	}

	fun addEdit(offset: Int, text: CharSequence, isInsert: Boolean) = addEdit(Edit(offset, text, isInsert))
	fun addEdit(edit: Edit) {
		undoStack.add(edit)
		redoStack.clear()
	}

	fun done() {
		if (null != undoStack.peekOrNull())
			undoStack.push(null)
	}

	private fun doneUndo() {
		if (null != redoStack.peekOrNull())
			redoStack.push(null)
	}
}
