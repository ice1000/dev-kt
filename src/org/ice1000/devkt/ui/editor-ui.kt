package org.ice1000.devkt.ui

import javax.swing.JTextPane

interface DevKtDocument<in TextAttributes> : AnnotationHolder<TextAttributes> {
	fun adjustFormat(offs: Int = 0, len: Int = length - offs)
	fun clear() = remove(0, length)
	fun remove(offs: Int, len: Int)
	fun insert(offs: Int, str: String)
	fun reparse()
}

class DevKtDocumentHandler<TextAttributes>(private val document: DevKtDocument<TextAttributes>) {
	fun insert(offs: Int, len: Int) {
	}
}

//FIXME: tab会被当做1个字符, 不知道有没有什么解决办法
fun JTextPane.lineColumnToPos(line: Int, column: Int = 1): Int {
	val lineStart = document.defaultRootElement.getElement(line - 1).startOffset
	return lineStart + column - 1
}

fun JTextPane.posToLineColumn(pos: Int): Pair<Int, Int> {
	val root = document.defaultRootElement
	val line = root.getElementIndex(pos)
	val column = pos - root.getElement(line).startOffset
	return line + 1 to column + 1
}

