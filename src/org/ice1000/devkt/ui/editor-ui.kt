package org.ice1000.devkt.ui

import org.ice1000.devkt.*
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.*
import org.ice1000.devkt.openapi.*
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import java.util.*

interface DevKtDocument<in TextAttributes> : LengthOwner {
	var caretPosition: Int
	var selectionStart: Int
	var selectionEnd: Int
	fun clear() = delete(0, length)
	fun delete(offs: Int, len: Int)
	fun insert(offs: Int, str: String?)
	fun changeCharacterAttributes(offset: Int, length: Int, s: TextAttributes, replace: Boolean)
	fun changeParagraphAttributes(offset: Int, length: Int, s: TextAttributes, replace: Boolean)
	fun resetLineNumberLabel(str: String)
	fun startOffsetOf(line: Int): Int
	fun endOffsetOf(line: Int): Int
	fun lineOf(offset: Int): Int
	fun lockWrite()
	fun unlockWrite()
	fun message(text: String)

	//FIXME: tab会被当做1个字符, 不知道有没有什么解决办法
	fun lineColumnToPos(line: Int, column: Int = 1) = startOffsetOf(line - 1) + column - 1

	fun posToLineColumn(pos: Int): Pair<Int, Int> {
		val line = lineOf(pos)
		val column = pos - startOffsetOf(line)
		return line + 1 to column + 1
	}
}

class DevKtDocumentHandler<TextAttributes>(
		internal val document: DevKtDocument<TextAttributes>,
		private val colorScheme: ColorScheme<TextAttributes>) :
		AnnotationHolder<TextAttributes> {
	private val undoManager = DevKtUndoManager()
	private var selfMaintainedString = StringBuilder()
	private val languages: MutableList<DevKtLanguage<TextAttributes>> = arrayListOf(
			Java(JavaAnnotator(), JavaSyntaxHighlighter()),
			Kotlin(KotlinAnnotator(), KotlinSyntaxHighlighter()),
			PlainText(PlainTextAnnotator(), PlainTextSyntaxHighlighter())
	)
	private val defaultLanguage = languages[1]
	fun startOffsetOf(line: Int) = document.startOffsetOf(line)
	fun endOffsetOf(line: Int) = document.endOffsetOf(line)
	fun lineOf(offset: Int) = document.lineOf(offset)
	var selectionStart
		get() = document.selectionStart
		set(value) {
			document.selectionStart = value
		}
	var selectionEnd
		get() = document.selectionEnd
		set(value) {
			document.selectionEnd = value
		}

	init {
		adjustFormat()
		ServiceLoader
				.load(ExtendedDevKtLanguage::class.java)
				.forEach {
					Analyzer.registerLanguage(it)
					handleException {
						@Suppress("UNCHECKED_CAST")
						languages += it as DevKtLanguage<TextAttributes>
					}
				}
	}

	private var currentLanguage: DevKtLanguage<TextAttributes>? = null
	private var psiFileCache: PsiFile? = null
	private val highlightCache = ArrayList<TextAttributes?>(5000)
	private var lineNumber = 1

	override val text get() = selfMaintainedString.toString()
	override fun getLength() = document.length
	val lineCommentStart get() = currentLanguage?.lineCommentStart
	val blockComment get() = currentLanguage?.blockComment
	val canUndo get() = undoManager.canUndo
	val canRedo get() = undoManager.canRedo

	fun textWithin(start: Int, end: Int): String = selfMaintainedString.substring(start, end)
	fun replaceText(regex: Regex, replacement: String) = selfMaintainedString.replace(regex, replacement)
	fun undo() = undoManager.undo(this)
	fun redo() = undoManager.redo(this)

	fun useDefaultLanguage() = switchLanguage(defaultLanguage)
	fun switchLanguage(fileName: String) {
		switchLanguage(languages.firstOrNull { it.satisfies(fileName) })
	}

	fun switchLanguage(language: DevKtLanguage<TextAttributes>?) {
		currentLanguage = language
	}

	val psiFile: PsiFile?
		get() = psiFileCache ?: currentLanguage?.run { Analyzer.parse(text, language) }

	fun adjustFormat(offs: Int = 0, len: Int = document.length - offs) {
		if (len <= 0) return
		document.changeParagraphAttributes(offs, len, colorScheme.tabSize, false)
		val currentLineNumber = selfMaintainedString.count { it == '\n' } + 1
		val change = currentLineNumber != lineNumber
		lineNumber = currentLineNumber
		//language=HTML
		if (change) document.resetLineNumberLabel((1..currentLineNumber).joinToString(
				separator = "<br/>", prefix = "<html>", postfix = "&nbsp;</html>"))
	}

	/**
	 * Delete without checking
	 *
	 * @param offset Int see [delete]
	 * @param length Int see [delete]
	 */
	fun deleteDirectly(offset: Int, length: Int, reparse: Boolean = true) {
		selfMaintainedString.delete(offset, offset + length)
		with(document) {
			delete(offset, length)
			if (reparse) {
				adjustFormat(offset, length)
				reparse()
			}
		}
	}

	/**
	 * Handles user input, delete with checks and undo recording
	 *
	 * @param offs Int see [insert]
	 * @param len Int length of deletion
	 */
	fun delete(offs: Int, len: Int) {
		val delString = selfMaintainedString.substring(offs, offs + len)
		if (delString.isEmpty()) return
		with(undoManager) {
			addEdit(offs, delString, false)
			done()
		}
		val char = delString[0]
		if (char in paired && selfMaintainedString.getOrNull(offs + 1) == paired[char]) {
			deleteDirectly(offs, 2)
		} else deleteDirectly(offs, len)
	}

	/**
	 * Clear the editor and set the content with undo recording
	 *
	 * @param string String new content.
	 */
	fun resetTextTo(string: String) {
		with(undoManager) {
			addEdit(0, selfMaintainedString, false)
			addEdit(0, string, true)
			done()
		}
		with(selfMaintainedString) {
			setLength(0)
			append(string)
		}
		with(document) {
			caretPosition = 0
			clear()
			insert(0, string)
			adjustFormat()
			reparse()
		}
	}

	/**
	 * Insert without checking
	 *
	 * @param offset Int see [insert]
	 * @param string String see [insert]
	 * @param move Int how long the caret should move
	 */
	fun insertDirectly(offset: Int, string: String, move: Int = string.length, reparse: Boolean = true) {
		selfMaintainedString.insert(offset, string)
		with(document) {
			insert(offset, string)
			caretPosition += move
			if (reparse) {
				reparse()
				adjustFormat(offset, string.length)
			}
		}
	}

	/**
	 * Handles user input, insert with checks and undo recording
	 *
	 * @param offs Int offset from the beginning of the document
	 * @param str String? text to insert
	 */
	fun insert(offs: Int, str: String?) {
		if (offs < 0) return
		val normalized = str?.filterNot { it == '\r' } ?: return
		with(undoManager) {
			addEdit(offs, normalized, true)
			done()
		}
		return if (normalized.length > 1)
			insertDirectly(offs, normalized, 0)
		else {
			val char = normalized[0]
			if (char in paired.values) {
				if (offs != 0
						&& selfMaintainedString.getOrNull(offs) == char) {
					insertDirectly(offs, "", 1)
				} else insertDirectly(offs, normalized, 0)
			} else if (char in paired) insertDirectly(offs, "$normalized${paired[char]}", -1)
			else insertDirectly(offs, normalized, 0)
		}
	}

	fun reparse() {
		val lang = currentLanguage ?: return
		while (highlightCache.size <= document.length) highlightCache.add(null)
		// val time = System.currentTimeMillis()
		if (GlobalSettings.highlightTokenBased) lex(lang)
		// val time2 = System.currentTimeMillis()
		if (GlobalSettings.highlightSemanticBased) parse(lang)
		// val time3 = System.currentTimeMillis()
		rehighlight()
		// benchmark
		// println("${time2 - time}, ${time3 - time2}, ${System.currentTimeMillis() - time3}")
	}

	private fun lex(language: DevKtLanguage<TextAttributes>) {
		Analyzer
				.lex(text, language.createLexer(Analyzer.project))
				.filter { it.type !in TokenSet.WHITE_SPACE }
				.forEach { (start, end, _, type) ->
					// println("$text in ($start, $end)")
					language.attributesOf(type, colorScheme)?.let {
						highlight(start, end, it)
					}
				}
	}

	/**
	 * @see com.intellij.lang.annotation.AnnotationHolder.createAnnotation
	 */
	override fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: TextAttributes) {
		if (tokenStart >= tokenEnd || tokenEnd >= length) return
		for (i in tokenStart until tokenEnd) highlightCache[i] = attributeSet
	}

	private fun parse(language: DevKtLanguage<TextAttributes>) {
		SyntaxTraverser
				.psiTraverser(Analyzer.parse(text, language.language).also { psiFileCache = it })
				.forEach { psi ->
					if (psi !is PsiWhiteSpace) language.annotate(psi, this, colorScheme)
				}
	}

	private fun rehighlight() {
		if (document.length > 1) try {
			document.lockWrite()
			var tokenStart = 0
			var attributeSet = highlightCache[0]
			highlightCache[0] = null
			for (i in 1 until highlightCache.size) {
				if (attributeSet != highlightCache[i]) {
					if (attributeSet != null)
						document.changeCharacterAttributes(tokenStart, i - tokenStart, attributeSet, true)
					tokenStart = i
					attributeSet = highlightCache[i]
				}
				highlightCache[i] = null
			}
		} finally {
			document.unlockWrite()
		}
	}

	fun nextLine() = with(document) {
		message("Started new line")
		val index = caretPosition
		val endOfLineIndex = selfMaintainedString.indexOf('\n', index)
		insert(if (endOfLineIndex < 0) length else endOfLineIndex, "\n")
		caretPosition = endOfLineIndex + 1
	}

	fun splitLine() = with(document) {
		message("Split new line")
		val index = caretPosition
		insert(index, "\n")
		caretPosition = index
	}

	fun newLineBeforeCurrent() = with(document) {
		message("Started new line before current line")
		val index = caretPosition
		val startOfLineIndex = selfMaintainedString
				.lastIndexOf('\n', (index - 1).coerceAtLeast(0))
				.coerceAtLeast(0)
		insert(startOfLineIndex, "\n")
		caretPosition = startOfLineIndex + 1
	}

	fun handleInsert(offs: Int, str: String?) {
		currentLanguage?.run {
			handleTyping(offs, str, psiFile?.findElementAt(offs), this@DevKtDocumentHandler)
		} ?: insert(offs, str)
	}
}
