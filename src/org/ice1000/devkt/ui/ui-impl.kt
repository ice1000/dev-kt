package org.ice1000.devkt.ui

import org.ice1000.devkt.Kotlin
import org.ice1000.devkt.`{-# LANGUAGE DevKt #-}`
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.js.translate.utils.splitToRanges
import org.jetbrains.kotlin.lexer.KtTokens.*
import java.awt.Desktop
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import java.net.URL
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.text.*
import javax.swing.undo.UndoManager


fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, AllIcons.KOTLIN)
}

/**
 * @author ice1000
 */
interface AnnotationHolder {
	val text: String
	fun resetTabSize()
	fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet)

	fun highlight(range: TextRange, attributeSet: AttributeSet) =
			highlight(range.startOffset, range.endOffset, attributeSet)

	fun highlight(astNode: ASTNode, attributeSet: AttributeSet) =
			highlight(astNode.textRange, attributeSet)

	fun highlight(element: PsiElement, attributeSet: AttributeSet) =
			highlight(element.textRange, attributeSet)
}

/**
 * @author ice1000
 * @since v0.0.1
 */
class UIImpl(private val frame: `{-# LANGUAGE DevKt #-}`) : UI() {
	val settings = frame.globalSettings
	private val undoManager = UndoManager()
	private var edited = false
		set(value) {
			val change = field != value
			field = value
			if (change) refreshTitle()
		}
	var currentFile: File? = null
		set(value) {
			val change = field != value
			field = value
			if (change) refreshTitle()
		}

	fun refreshTitle() {
		frame.title = buildString {
			if (edited) append("*")
			append(currentFile?.absolutePath ?: "Untitled")
			append(" - ")
			append(settings.appName)
		}
	}

	internal lateinit var undoMenuItem: JMenuItem
	internal lateinit var saveMenuItem: JMenuItem
	internal lateinit var redoMenuItem: JMenuItem
	internal lateinit var showInFilesMenuItem: JMenuItem
	private val document: KtDocument

	private inner class KtDocument : DefaultStyledDocument(), AnnotationHolder {
		private val highlightCache = ArrayList<AttributeSet?>(5000)
		private val colorScheme = ColorScheme(settings, attributeContext)
		private val annotator = KotlinAnnotator()
		private val stringTokens = TokenSet.create(
				OPEN_QUOTE,
				CLOSING_QUOTE,
				REGULAR_STRING_PART
		)

		private val stringTemplateTokens = TokenSet.create(
				SHORT_TEMPLATE_ENTRY_START,
				LONG_TEMPLATE_ENTRY_START,
				LONG_TEMPLATE_ENTRY_END
		)

		init {
			addUndoableEditListener {
				undoManager.addEdit(it.edit)
				edited = true
				updateUndoMenuItems()
			}
			resetTabSize()
		}

		override fun resetTabSize() = setParagraphAttributes(0, length, colorScheme.tabSize, false)
		override val text get() = editor.text

		override fun insertString(offs: Int, str: String, a: AttributeSet) {
			super.insertString(offs, str, a)
			reparse()
			resetTabSize()
		}

		override fun remove(offs: Int, len: Int) {
			super.remove(offs, len)
			reparse()
		}

		private fun lex() {
			val tokens = Kotlin.lex(text)
			for ((start, end, text, type) in tokens)
				attributesOf(type)?.let { highlight(start, end, it) }
		}

		private fun parse() {
			val ktFile = Kotlin.parse(text) ?: return
			SyntaxTraverser.psiTraverser(ktFile).forEach { psi ->
				if (psi !is PsiWhiteSpace) annotator.annotate(psi, this, colorScheme)
			}
		}

		fun reparse() {
			while (highlightCache.size <= length) highlightCache.add(null)
			// val time = System.currentTimeMillis()
			if (settings.highlightTokenBased) lex()
			// val time2 = System.currentTimeMillis()
			if (settings.highlightSemanticBased) parse()
			// val time3 = System.currentTimeMillis()
			if (length > 1) {
				var tokenStart = 0
				var attributeSet = highlightCache[0]
				highlightCache[0] = null
				for (i in 1 until highlightCache.size) {
					if (attributeSet != highlightCache[i]) {
						if (attributeSet != null)
							setCharacterAttributes(tokenStart, i - tokenStart, attributeSet, true)
						tokenStart = i
						attributeSet = highlightCache[i]
					}
					highlightCache[i] = null
				}
			}
			// benchmark
			// println("${time2 - time}, ${time3 - time2}, ${System.currentTimeMillis() - time2}")
		}

		/**
		 * @see com.intellij.openapi.fileTypes.SyntaxHighlighter.getTokenHighlights
		 */
		private fun attributesOf(type: IElementType) = when (type) {
			IDENTIFIER -> colorScheme.identifiers
			CHARACTER_LITERAL -> colorScheme.charLiteral
			EOL_COMMENT -> colorScheme.lineComments
			DOC_COMMENT -> colorScheme.docComments
			SEMICOLON -> colorScheme.semicolon
			COLON -> colorScheme.colon
			COMMA -> colorScheme.comma
			INTEGER_LITERAL, FLOAT_LITERAL -> colorScheme.numbers
			LPAR, RPAR -> colorScheme.parentheses
			LBRACE, RBRACE -> colorScheme.braces
			LBRACKET, RBRACKET -> colorScheme.brackets
			BLOCK_COMMENT, SHEBANG_COMMENT -> colorScheme.blockComments
			in stringTokens -> colorScheme.string
			in stringTemplateTokens -> colorScheme.templateEntries
			in KEYWORDS -> colorScheme.keywords
			in OPERATIONS -> colorScheme.operators
			else -> null
		}

		override fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet) =
				doHighlight(tokenStart, tokenEnd, attributeSet)

		/**
		 * @see com.intellij.lang.annotation.AnnotationHolder.createAnnotation
		 */
		fun doHighlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet) {
			if (tokenStart >= tokenEnd) return
			for (i in tokenStart until tokenEnd) {
				highlightCache[i] = attributeSet
			}
		}
	}

	init {
		frame.jMenuBar = menuBar
		mainMenu(menuBar, frame)
		document = KtDocument()
		editor.document = document
	}

	fun postInit() {
		updateUndoMenuItems()
		val lastOpenedFile = File(settings.lastOpenedFile)
		if (lastOpenedFile.canRead()) {
			edited = false
			loadFile(lastOpenedFile)
			edited = false // TODO replace with direct text operation
		}
		editor.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				if (e.isControlDown && !e.isAltDown && !e.isShiftDown && e.keyCode == KeyEvent.VK_Z) undo()
				if (e.isControlDown && !e.isAltDown && !e.isShiftDown && e.keyCode == KeyEvent.VK_S) save()
				if (e.isControlDown && e.isAltDown && !e.isShiftDown && e.keyCode == KeyEvent.VK_Y) sync()
				if (e.isControlDown && !e.isAltDown && e.isShiftDown && e.keyCode == KeyEvent.VK_Z) redo()
			}
		})
	}

	fun settings() {
		frame.TODO()
		reloadSettings()
	}

	fun sync() {
		currentFile?.let(::loadFile)
	}

	fun save() {
		val file = currentFile ?: JFileChooser(settings.recentFiles.firstOrNull()?.parentFile).apply {
			showSaveDialog(mainPanel)
		}.selectedFile ?: return
		currentFile = file
		if (!file.exists()) file.createNewFile()
		settings.recentFiles.add(file)
		file.writeText(editor.text)
		edited = false
	}

	fun createNewFile(templateName: String) {
		if (!makeSureLeaveCurrentFile()) {
			currentFile = null
			edited = true
			editor.text = javaClass
					.getResourceAsStream("/template/$templateName")
					.reader()
					.readText()
		}
	}

	fun open() {
		JFileChooser(currentFile?.parentFile).apply {
			// dialogTitle = "Choose a Kotlin file"
			fileFilter = kotlinFileFilter
			showOpenDialog(mainPanel)
		}.selectedFile?.let {
			loadFile(it)
			settings.recentFiles.add(it)
		}
	}

	fun loadFile(it: File) {
		if (it.canRead() and !makeSureLeaveCurrentFile()) {
			currentFile = it
			val path = it.absolutePath.orEmpty()
			editor.text = it.readText()
			edited = false
			settings.lastOpenedFile = path
		}
		updateShowInFilesMenuItem()
	}

	fun idea() = browse("https://www.jetbrains.com/idea/download/")
	fun clion() = browse("https://www.jetbrains.com/clion/download/")
	fun eclipse() = browse("http://marketplace.eclipse.org/content/kotlin-plugin-eclipse")
	fun emacs() = browse("https://melpa.org/#/kotlin-mode")

	private fun browse(url: String) = try {
		Desktop.getDesktop().browse(URL(url).toURI())
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when browsing $url:\n${e.message}")
	}

	private fun open(file: File) = try {
		Desktop.getDesktop().open(file)
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when opening ${file.absolutePath}:\n${e.message}")
	}

	fun buildAsClasses() {
		Kotlin.parse(editor.text)?.let(Kotlin::compile)
	}

	fun buildAsJs() {
		frame.TODO()
	}

	fun makeSureLeaveCurrentFile() = edited && JOptionPane.YES_OPTION !=
			JOptionPane.showConfirmDialog(
					mainPanel,
					"${currentFile?.name ?: "Current file"} unsaved, leave?",
					UIManager.getString("OptionPane.titleText"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					AllIcons.KOTLIN)

	fun buildAndRun() {
		buildAsClasses()
		justRun()
		frame.TODO()
	}

	fun justRun() {
		frame.TODO()
	}

	fun buildAsJar() {
		frame.TODO()
	}

	fun exit() {
		settings.save()
		if (!makeSureLeaveCurrentFile()) {
			frame.dispose()
			System.exit(0)
		}
	}

	fun updateUndoMenuItems() {
		undoMenuItem.isEnabled = undoManager.canUndo()
		redoMenuItem.isEnabled = undoManager.canRedo()
		saveMenuItem.isEnabled = edited
	}

	fun updateShowInFilesMenuItem() {
		showInFilesMenuItem.isEnabled = currentFile != null
	}

	fun showInFiles() {
		currentFile?.run { open(parentFile) }
	}

	fun undo() {
		if (undoManager.canUndo()) {
			undoManager.undo()
			edited = true
		}
	}

	fun redo() {
		if (undoManager.canRedo()) {
			undoManager.redo()
			edited = true
		}
	}

	fun selectAll() = editor.selectAll()
	fun cut() = editor.cut()
	fun copy() = editor.copy()
	fun paste() = editor.paste()

	fun reloadSettings() {
		frame.bounds = settings.windowBounds
		refreshTitle()
		with(document) {
			resetTabSize()
			reparse()
		}
	}
}
