package org.ice1000.devkt.ui

import org.ice1000.devkt.config.ColorScheme
import org.ice1000.devkt.lang.JavaAnnotator
import org.ice1000.devkt.lang.KotlinAnnotator
import org.ice1000.devkt.paired
import javax.swing.JTextPane

interface DevKtDocument<in TextAttributes> : AnnotationHolder<TextAttributes> {
	var caretPosition: Int
	fun adjustFormat(offs: Int = 0, len: Int = length - offs)
	fun clear() = remove(0, length)
	fun remove(offs: Int, len: Int)
	fun insert(offs: Int, str: String)
	fun reparse()
}

class DevKtDocumentHandler<TextAttributes>(
		private val document: DevKtDocument<TextAttributes>,
		private val colorScheme: ColorScheme<TextAttributes>) {
	private var selfMaintainedString = StringBuilder()
	private val ktAnotator = KotlinAnnotator<TextAttributes>()
	private val javaAnotator = JavaAnnotator<TextAttributes>()
	private val highlightCache = ArrayList<TextAttributes?>(5000)

	val text get() = selfMaintainedString.toString()

	fun clear() = remove(0, document.length)
	fun remove(offs: Int, len: Int) {
		val delString = this.text.substring(offs, offs + len)        //即将被删除的字符串
		val (offset, length) = when {
			delString in paired            //是否存在于字符对里
					&& text.getOrNull(offs + 1)?.toString() == paired[delString] -> {
				offs to 2
			}
			else -> offs to len
		}

		selfMaintainedString.delete(offset, offset + length)
		with(document) {
			remove(offset, length)
			reparse()
			adjustFormat(offset, length)
		}
	}

	fun insert(offs: Int, str: String) {
		val normalized = str.filterNot { it == '\r' }
		val (offset, string, move) = when {
			normalized.length > 1 -> Triple(offs, normalized, 0)
			normalized in paired.values -> {
				val another = paired.keys.first { paired[it] == normalized }
				if (offs != 0
						&& selfMaintainedString.substring(offs - 1, 1) == another
						&& selfMaintainedString.substring(offs, 1) == normalized) {
					Triple(offs, "", 1)
				} else Triple(offs, normalized, 0)
			}
			normalized in paired -> Triple(offs, normalized + paired[normalized], -1)
			else -> Triple(offs, normalized, 0)
		}
		selfMaintainedString.insert(offset, string)
		with(document) {
			insert(offset, string)
			caretPosition += move
			reparse()
			adjustFormat(offset, string.length)
		}
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

