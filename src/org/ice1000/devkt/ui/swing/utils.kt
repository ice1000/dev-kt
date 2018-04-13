package org.ice1000.devkt.ui.swing

import org.ice1000.devkt.ui.DevKtIcons
import javax.swing.*

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

fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, DevKtIcons.KOTLIN)
}
