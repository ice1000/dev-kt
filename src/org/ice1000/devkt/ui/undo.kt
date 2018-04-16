package org.ice1000.devkt.ui

import org.jetbrains.kotlin.com.intellij.util.containers.Stack

sealed class Do
data class Insert(val offset: Int, val text: String) : Do()
data class Delete(val offset: Int, val length: Int) : Do()
object Mark : Do()

class UndoManager {
	private val stack = Stack<Do>()
	private val redostack = Stack<Do>()
	fun undo() {
	}
	fun doneInsersion() {
	}
}
