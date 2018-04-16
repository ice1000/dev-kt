package org.ice1000.devkt.ui

import org.ice1000.devkt.*
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.*
import org.ice1000.devkt.openapi.ColorScheme
import org.ice1000.devkt.openapi.ExtendedDevKtLanguage
import org.ice1000.devkt.openapi.ui.IDevKtDocument
import org.ice1000.devkt.openapi.ui.IDevKtDocumentHandler
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import java.util.*

interface DevKtDocument<in TextAttributes> : IDevKtDocument<TextAttributes> {
	override fun clear() = delete(0, length)
	var edited: Boolean

	//FIXME: tab会被当做1个字符, 不知道有没有什么解决办法
	fun lineColumnToPos(line: Int, column: Int = 1) = startOffsetOf(line - 1) + column - 1

	fun posToLineColumn(pos: Int): Pair<Int, Int> {
		val line = lineOf(pos)
		val column = pos - startOffsetOf(line)
		return line + 1 to column + 1
	}
}

class DevKtDocumentHandler<TextAttributes>(
		val document: DevKtDocument<TextAttributes>,
		private val colorScheme: ColorScheme<TextAttributes>) :
		IDevKtDocumentHandler<TextAttributes> {
	private val undoManager = DevKtUndoManager()
	private var selfMaintainedString = StringBuilder()
	private val languages: MutableList<DevKtLanguage<TextAttributes>> = arrayListOf(
			Java(JavaAnnotator(), JavaSyntaxHighlighter()),
			Kotlin(KotlinAnnotator(), KotlinSyntaxHighlighter()),
			PlainText(PlainTextAnnotator(), PlainTextSyntaxHighlighter())
	)
	private val defaultLanguage = languages[1]
	override fun startOffsetOf(line: Int) = document.startOffsetOf(line)
	override fun endOffsetOf(line: Int) = document.endOffsetOf(line)
	override fun lineOf(offset: Int) = document.lineOf(offset)
	override var selectionStart
		get() = document.selectionStart
		set(value) {
			document.selectionStart = value
		}
	override var selectionEnd
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
	override val canUndo get() = undoManager.canUndo
	override val canRedo get() = undoManager.canRedo

	override fun textWithin(start: Int, end: Int): String = selfMaintainedString.substring(start, end)
	override fun replaceText(regex: Regex, replacement: String) = selfMaintainedString.replace(regex, replacement)
	override fun undo() {
		document.edited = true
		undoManager.undo(this)
	}

	override fun redo() {
		document.edited = true
		undoManager.redo(this)
	}

	override fun done() = undoManager.done()
	override fun clearUndo() = undoManager.clear()
	override fun addEdit(offset: Int, text: CharSequence, isInsert: Boolean) = addEdit(Edit(offset, text, isInsert))
	override fun addEdit(edit: Edit) {
		document.edited = true
		undoManager.addEdit(edit)
	}

	override fun useDefaultLanguage() = switchLanguage(defaultLanguage)
	override fun switchLanguage(fileName: String) {
		switchLanguage(languages.firstOrNull { it.satisfies(fileName) })
	}

	override fun switchLanguage(language: DevKtLanguage<TextAttributes>?) {
		currentLanguage = language
	}

	val psiFile: PsiFile?
		get() = psiFileCache ?: currentLanguage?.run { Analyzer.parse(text, language) }

	override fun adjustFormat(offs: Int, len: Int) {
		if (len <= 0) return
		document.changeParagraphAttributes(offs, len, colorScheme.tabSize, false)
		val currentLineNumber = selfMaintainedString.count { it == '\n' } + 1
		val change = currentLineNumber != lineNumber
		lineNumber = currentLineNumber
		//language=HTML
		if (change) document.resetLineNumberLabel((1..currentLineNumber).joinToString(
				separator = "<br/>", prefix = "<html>", postfix = "&nbsp;</html>"))
	}

	fun commentCurrent() {
		val lines = lineOf(selectionStart)..lineOf(selectionEnd)
		val lineCommentStart = lineCommentStart ?: return
		val add = lines.any {
			val lineStart = startOffsetOf(it)
			val lineEnd = endOffsetOf(it)
			val lineText = textWithin(lineStart, lineEnd)
			!lineText.startsWith(lineCommentStart)
		}
		lines.forEach {
			val lineStart = startOffsetOf(it)
			addEdit(lineStart, lineCommentStart, add)
			if (add) insertDirectly(lineStart, lineCommentStart)
			else deleteDirectly(lineStart, lineCommentStart.length)
		}
		done()
	}

	fun blockComment() {
		val (start, end) = blockComment ?: return
		val selectionStart = selectionStart
		addEdit(selectionEnd, end, true)
		addEdit(selectionStart, start, true)
		insertDirectly(selectionEnd, end, 0)
		insertDirectly(selectionStart, start, 0)
		done()
	}

	/**
	 * Delete without checking
	 *
	 * @param offset Int see [delete]
	 * @param length Int see [delete]
	 */
	override fun deleteDirectly(offset: Int, length: Int, reparse: Boolean) {
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
	override fun delete(offs: Int, len: Int) {
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
	override fun resetTextTo(string: String) {
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
	override fun insertDirectly(offset: Int, string: String, move: Int, reparse: Boolean) {
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
	override fun insert(offs: Int, str: String?) {
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

	override fun reparse() {
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
					highlight(start, end, language.attributesOf(type, colorScheme) ?: colorScheme.default)
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
		val start = if (endOfLineIndex < 0) length else endOfLineIndex
		addEdit(start, "\n", true)
		insert(start, "\n")
		done()
		caretPosition = endOfLineIndex + 1
	}

	fun splitLine() = with(document) {
		message("Split new line")
		val index = caretPosition
		addEdit(index, "\n", true)
		insert(index, "\n")
		done()
		caretPosition = index
	}

	fun newLineBeforeCurrent() = with(document) {
		message("Started new line before current line")
		val index = caretPosition
		val startOfLineIndex = selfMaintainedString
				.lastIndexOf('\n', (index - 1).coerceAtLeast(0))
				.coerceAtLeast(0)
		addEdit(startOfLineIndex, "\n", true)
		insert(startOfLineIndex, "\n")
		done()
		caretPosition = startOfLineIndex + 1
	}

	fun handleInsert(offs: Int, str: String?) {
		currentLanguage?.run {
			handleTyping(offs, str, psiFile?.findElementAt(offs), this@DevKtDocumentHandler)
		} ?: insert(offs, str)
	}
}
