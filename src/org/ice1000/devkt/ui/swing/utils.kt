package org.ice1000.devkt.ui.swing

import org.ice1000.devkt.ui.DevKtDocument
import org.ice1000.devkt.ui.DevKtIcons
import javax.swing.*

//FIXME: tab会被当做1个字符, 不知道有没有什么解决办法
fun DevKtDocument<*>.lineColumnToPos(line: Int, column: Int = 1) = startOffsetOf(line - 1) + column - 1
fun DevKtDocument<*>.posToLineColumn(pos: Int): Pair<Int, Int> {
	val line = lineOf(pos)
	val column = pos - startOffsetOf(line)
	return line + 1 to column + 1
}

fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, DevKtIcons.KOTLIN)
}
