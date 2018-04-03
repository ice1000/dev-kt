package org.ice1000.devkt.ui

import org.ice1000.devkt.*
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.*
import java.awt.Desktop
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import java.net.URL
import javax.swing.*
import javax.swing.text.AttributeSet
import javax.swing.text.DefaultStyledDocument
import javax.swing.undo.UndoManager


fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, AllIcons.KOTLIN)
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

	private fun refreshTitle() {
		frame.title = buildString {
			if (edited) append("*")
			append(currentFile?.absolutePath ?: "Untitled")
			append(" - ")
			append(frame.defaultTitle)
		}
	}

	internal lateinit var undoMenuItem: JMenuItem
	internal lateinit var saveMenuItem: JMenuItem
	internal lateinit var redoMenuItem: JMenuItem
	internal lateinit var showInFilesMenuItem: JMenuItem

	private inner class KtDocument : DefaultStyledDocument() {
		private val colorScheme = ColorScheme(settings)
		private val stringTokens = TokenSet.create(
				KtTokens.OPEN_QUOTE,
				KtTokens.CLOSING_QUOTE,
				KtTokens.REGULAR_STRING_PART)

		init {
			addUndoableEditListener {
				undoManager.addEdit(it.edit)
				edited = true
				updateUndoMenuItems()
			}
			resetProperties()
		}

		fun resetProperties() {
			setParagraphAttributes(0, length, colorScheme.tabSize, false)
		}

		override fun insertString(offs: Int, str: String, a: AttributeSet) {
			super.insertString(offs, str, a)
			reparse()
			resetProperties()
		}

		override fun remove(offs: Int, len: Int) {
			super.remove(offs, len)
			reparse()
		}

		private fun reparse() {
			val tokens = Kotlin.lex(editor.text)
			for ((start, end, text, type) in tokens)
				highlight(start, end, attributesOf(type))
		}

		private fun attributesOf(type: IElementType) = when (type) {
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
			in stringTokens -> colorScheme.string
			in COMMENTS -> colorScheme.blockComments
			in KEYWORDS -> colorScheme.keywords
			in OPERATIONS -> colorScheme.operators
			else -> colorScheme.others
		}

		private fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet) {
			setCharacterAttributes(tokenStart, tokenEnd - tokenStart, attributeSet, false)
		}
	}

	init {
		frame.jMenuBar = menuBar
		mainMenu(menuBar, frame)
		editor.document = KtDocument()
		updateUndoMenuItems()
		val lastOpenedFile = File(settings.lastOpenedFile)
		if (lastOpenedFile.canRead()) {
			edited = false
			loadFile(lastOpenedFile)
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
					.getResourceAsStream("/template/$templateName.kt")
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

	private fun makeSureLeaveCurrentFile() = edited && JOptionPane.YES_OPTION !=
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
		currentFile?.let(::open)
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
}
