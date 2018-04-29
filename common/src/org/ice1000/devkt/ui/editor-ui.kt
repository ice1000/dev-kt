package org.ice1000.devkt.ui

import com.bennyhuo.kotlin.opd.delegator
import org.ice1000.devkt.ASTToken
import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.*
import org.ice1000.devkt.openapi.ColorScheme
import org.ice1000.devkt.openapi.ExtendedDevKtLanguage
import org.ice1000.devkt.openapi.ui.*
import org.ice1000.devkt.openapi.util.*
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.util.*

interface DevKtDocument<TextAttributes> : IDevKtDocument<TextAttributes> {
	@JvmDefault
	override fun clear() = delete(0, length)

	//FIXME: tab会被当做1个字符, 不知道有没有什么解决办法
	@JvmDefault
	fun lineColumnToPos(line: Int, column: Int = 1) = startOffsetOf(line - 1) + column - 1

	@JvmDefault
	fun posToLineColumn(pos: Int): Pair<Int, Int> {
		val line = lineOf(pos)
		val column = pos - startOffsetOf(line)
		return line + 1 to column + 1
	}
}

class DevKtDocumentHandler<TextAttributes>(
		override val document: DevKtDocument<TextAttributes>,
		val window: DevKtWindow,
		private val colorScheme: ColorScheme<TextAttributes>) :
		IDevKtDocumentHandler<TextAttributes> {
	private val undoManager = DevKtUndoManager()
	private var selfMaintainedString = StringBuilder()
	override fun startOffsetOf(line: Int) = document.startOffsetOf(line)
	override fun endOffsetOf(line: Int) = document.endOffsetOf(line)
	override fun lineOf(offset: Int) = document.lineOf(offset)
	override var selectionStart by document::selectionStart.delegator()
	override var selectionEnd by document::selectionEnd.delegator()

	private val plainTextLanguage = PlainText<TextAttributes>()

	val languages = listOf<DevKtLanguage<TextAttributes>>(
			Java(), Kotlin(), plainTextLanguage
	) + ServiceLoader
			.load(ExtendedDevKtLanguage::class.java)
			.mapNotNull {
				Analyzer.registerLanguage(it)
				handleException {
					@Suppress("UNCHECKED_CAST")
					return@mapNotNull it as DevKtLanguage<TextAttributes>
				}
				null
			}

	init {
		adjustFormat()
	}

	private var currentLanguage: DevKtLanguage<TextAttributes> = plainTextLanguage
	private var psiFileCache: PsiFile? = null
	private val highlightCache = ArrayList<TextAttributes?>(5000)
	private var lineNumber = 1
	override val text get() = selfMaintainedString.toString()
	override fun getLength() = document.length
	private val lineCommentStart get() = currentLanguage.lineCommentStart
	private val blockComment get() = currentLanguage.blockComment
	private var initialCompletionList: Set<CompletionElement> = emptySet()
	private val lexicalCompletionList: MutableSet<CompletionElement> = hashSetOf()
	override val canUndo get() = undoManager.canUndo
	override val canRedo get() = undoManager.canRedo

	private var currentTypingNodeCache: ASTToken? = null
	override val currentTypingNode: ASTToken?
		get() {
			val caretPosition = document.caretPosition
			if (caretPosition == currentTypingNodeCache?.end)
				return currentTypingNodeCache
			var currentNode =
					psiFile?.findElementAt(caretPosition) ?: return null
			while (caretPosition == currentNode.startOffset &&
					currentNode.prevSibling != null) {
				currentNode = currentNode.prevSibling
			}
			while (currentNode.lastChild != null) currentNode = currentNode.lastChild
			return currentNode
					.let(::ASTToken)
					.also { currentTypingNodeCache = it }
		}

	override fun textWithin(start: Int, end: Int): String = selfMaintainedString.substring(start, end)
	override fun replaceText(regex: Regex, replacement: String) = selfMaintainedString.replace(regex, replacement)
	override fun undo() {
		window.edited = true
		undoManager.undo(this)
	}

	override fun redo() {
		window.edited = true
		undoManager.redo(this)
	}

	override fun done() = undoManager.done()
	override fun clearUndo() = undoManager.clear()
	override fun addEdit(offset: Int, text: CharSequence, isInsert: Boolean) = addEdit(Edit(offset, text, isInsert))
	override fun addEdit(edit: Edit) {
		window.edited = true
		undoManager.addEdit(edit)
	}

	override fun switchLanguage(fileName: String) {
		switchLanguage(languages.firstOrNull { it.satisfies(fileName) } ?: return)
	}

	override fun switchLanguage(language: DevKtLanguage<TextAttributes>) {
		currentLanguage = language
		document.onChangeLanguage(language)
		initialCompletionList = language.initialCompletionElementList
		reparse()
	}

	val psiFile: PsiFile?
		get() = psiFileCache ?: currentLanguage.run { Analyzer.parse(text, language) }

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
			val lineText = textWithin(lineStart, lineEnd - 1)
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
		document.delete(offset, length)
		if (reparse) {
			adjustFormat(offset, length)
			reparse()
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
		val char = delString[0]
		val (offset, length) = if (char in paired && selfMaintainedString.getOrNull(offs + 1) == paired[char]) {
			Pair(offs, 2)
		} else Pair(offs, len)
		with(undoManager) {
			addEdit(offset, selfMaintainedString.subSequence(offset, offset + length), false)
			deleteDirectly(offset, length)
			done()
		}
		// window.doAsync {
		reparse()
		adjustFormat(offs, length)
		// }
	}

	/**
	 * Clear the editor and set the content with undo recording
	 *
	 * @param string String new content.
	 */
	override fun resetTextTo(string: String) {
		window.message("Text reset.")
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
		}
		adjustFormat()
		reparse()
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
		}
		if (reparse) {
			reparse()
			adjustFormat(offset, string.length)
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

		//Pairs of character completion
		val (offset, text, move) = if (normalized.length > 1)
			Triple(offs, normalized, 0)
		else {
			val char = normalized[0]
			if (char in paired.values) {
				if (offs != 0
						&& selfMaintainedString.getOrNull(offs) == char) {
					Triple(offs, "", 1)
				} else Triple(offs, normalized, 0)
			} else if (char in paired)
				Triple(offs, "$normalized${paired[char]}", -1)
			else {
				val value = insteadPaired[char]
				if (value != null) Triple(offs, value.value, 0)
				else Triple(offs, normalized, 0)
			}
		}

		with(undoManager) {
			insertDirectly(offset, text, move, reparse = false)
			addEdit(offset, text, true)
			done()
		}

		//Completion
		// window.doAsync {
		reparse()
		adjustFormat(offs, length)
		val currentNode = currentTypingNode ?: return
		if (currentLanguage.invokeAutoPopup(currentNode, normalized) && GlobalSettings.useCompletion) {
			val caretPosition = document.caretPosition
			window.uiThread { showCompletion(currentNode, caretPosition) }
		}
		// }
	}

	fun showCompletion() {
		showCompletion(currentTypingNode ?: return, document.caretPosition)
	}

	private fun showCompletion(currentNode: ASTToken, caretPosition: Int) {
		val currentText = currentNode.text.subSequence(0, caretPosition - currentNode.start)
		val completions = (initialCompletionList + lexicalCompletionList)
				.filter { it.lookup.startsWith(currentText, true) && it.text != currentText }
		if (completions.isNotEmpty()) window.createCompletionPopup(completions).show()
	}

	// TODO: make async
	override fun reparse(rehighlight: Boolean) {
		while (highlightCache.size <= document.length) highlightCache.add(null)
		val collected = mutableListOf<CompletionElement>()
		if (GlobalSettings.highlightTokenBased) lex(currentLanguage, collected)
		if (GlobalSettings.highlightSemanticBased) parse(currentLanguage, collected)
		lexicalCompletionList.clear()
		lexicalCompletionList.addAll(collected)
		if (rehighlight) rehighlight()
	}

	private fun lex(
			language: DevKtLanguage<TextAttributes>,
			collected: MutableList<CompletionElement>) {
		val caretPosition = document.caretPosition
		Analyzer
				.lex(selfMaintainedString, language.createLexer(Analyzer.project))
				.filter { it.type !in TokenSet.WHITE_SPACE }
				.forEach { node ->
					val (start, end, text, type) = node
					// println("$text in ($start, $end)")
					if (start < caretPosition) currentTypingNodeCache = node
					if (text.isNotBlank() && text.length > 1)
						collected += CompletionElement(text.trim(), type = "Token")
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

	private fun parse(
			language: DevKtLanguage<TextAttributes>,
			collected: MutableList<CompletionElement>) {
		SyntaxTraverser
				.psiTraverser(Analyzer.parse(text, language.language).also { psiFileCache = it })
				.filter { it !is PsiWhiteSpace }
				.forEach { psi ->
					if (currentLanguage.shouldAddAsCompletion(psi))
						collected += CompletionElement(psi.text)
					language.annotate(psi, this, colorScheme)
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
		window.message("Started new line")
		val index = caretPosition
		val endOfLineIndex = selfMaintainedString.indexOf('\n', index)
		val start = if (endOfLineIndex < 0) length else endOfLineIndex
		addEdit(start, "\n", true)
		insert(start, "\n")
		done()
		caretPosition = endOfLineIndex + 1
	}

	fun splitLine() = with(document) {
		window.message("Split new line")
		val index = caretPosition
		addEdit(index, "\n", true)
		insert(index, "\n")
		done()
		caretPosition = index
	}

	fun newLineBeforeCurrent() = with(document) {
		window.message("Started new line before current line")
		val index = caretPosition
		val startOfLineIndex = selfMaintainedString
				.lastIndexOf('\n', (index - 1).coerceAtLeast(0))
				.coerceAtLeast(0)
		addEdit(startOfLineIndex, "\n", true)
		insert(startOfLineIndex, "\n")
		done()
		caretPosition = startOfLineIndex + 1
	}

	fun handleInsert(str: String?) = handleInsert(document.caretPosition, str)
	fun handleInsert(offs: Int, str: String?) {
		currentLanguage.handleTyping(offs, str, psiFile?.findElementAt(offs), this)
	}
}
