package org.ice1000.devkt.ui.swing

import charlie.gensokyo.show
import net.iharder.dnd.FileDrop
import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.loadFont
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.config.swingColorScheme
import org.ice1000.devkt.ui.DevKtDocument
import org.ice1000.devkt.ui.DevKtDocumentHandler
import org.jetbrains.kotlin.psi.KtFile
import java.awt.Font
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.UndoableEditEvent
import javax.swing.text.*
import javax.swing.undo.UndoManager

/**
 * @author ice1000
 * @since v0.0.1
 */
class UIImpl(frame: DevKtFrame) : AbstractUI(frame) {
	private val undoManager = UndoManager()
	private var edited = false
		set(value) {
			val change = field != value
			field = value
			if (change) refreshTitle()
		}

	internal lateinit var saveMenuItem: JMenuItem
	internal lateinit var showInFilesMenuItem: JMenuItem
	internal lateinit var buildMenuBar: JMenu
	private val document: DevKtDocumentHandler<AttributeSet>

	private inner class KtDocument : DefaultStyledDocument(), DevKtDocument<AttributeSet> {
		private val root = defaultRootElement
		override var caretPosition
			get() = editor.caretPosition
			set(value) {
				editor.caretPosition = value
			}
		override var selectionEnd: Int
			get() = editor.selectionEnd
			set(value) {
				editor.selectionEnd = value
			}
		override var selectionStart: Int
			get() = editor.selectionStart
			set(value) {
				editor.selectionStart = value
			}

		init {
			addUndoableEditListener {
				if (it.source !== this) return@addUndoableEditListener
				undoManager.addEdit(it.edit)
				edited = true
			}
		}

		fun createHandler() = DevKtDocumentHandler(this, swingColorScheme(GlobalSettings, attributeContext))
		override fun startOffsetOf(line: Int) = root.getElement(line).startOffset
		override fun endOffsetOf(line: Int) = root.getElement(line).endOffset
		override fun lineOf(offset: Int) = root.getElementIndex(offset)

		override fun lockWrite() = writeLock()
		override fun unlockWrite() = writeUnlock()
		/** from [DevKtDocument] */
		override fun insert(offs: Int, str: String?) = super.insertString(offs, str, null)

		/** from [DevKtDocument] */
		override fun delete(offs: Int, len: Int) = super.remove(offs, len)

		/** from [DefaultStyledDocument] */
		override fun remove(offs: Int, len: Int) = document.delete(offs, len)

		/** from [DefaultStyledDocument] */
		override fun insertString(offs: Int, str: String?, a: AttributeSet?) = document.insert(offs, str)

		override fun resetLineNumberLabel(str: String) {
			lineNumberLabel.text = str
		}

		override fun message(text: String) {
			messageLabel.text = text
		}

		/**
		 * Re-implement of [setCharacterAttributes], invoke [fireUndoableEditUpdate] with
		 * [document] as event source, which is used by [undoManager] to prevent color
		 * modifications to be recorded.
		 */
		override fun changeCharacterAttributes(offset: Int, length: Int, s: AttributeSet, replace: Boolean) {
			val changes = DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE)
			buffer.change(offset, length, changes)
			val sCopy = s.copyAttributes()
			var lastEnd: Int
			var pos = offset
			while (pos < offset + length) {
				val run = getCharacterElement(pos)
				lastEnd = run.endOffset
				if (pos == lastEnd) break
				val attr = run.attributes as MutableAttributeSet
				changes.addEdit(AttributeUndoableEdit(run, sCopy, replace))
				if (replace) attr.removeAttributes(attr)
				attr.addAttributes(s)
				pos = lastEnd
			}
			changes.end()
			fireChangedUpdate(changes)
			fireUndoableEditUpdate(UndoableEditEvent(document, changes))
		}

		/**
		 * Re-implement of [setParagraphAttributes], invoke [fireUndoableEditUpdate] with
		 * [GlobalSettings] as event source, which is used by [undoManager] to prevent color
		 * modifications to be recorded.
		 */
		override fun changeParagraphAttributes(offset: Int, length: Int, s: AttributeSet, replace: Boolean) = try {
			writeLock()
			val changes = DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE)
			val sCopy = s.copyAttributes()
			for (i in root.getElementIndex(offset)..root.getElementIndex(offset + if (length > 0) length - 1 else 0)) {
				val paragraph = root.getElement(i)
				val attr = paragraph.attributes as MutableAttributeSet
				changes.addEdit(AttributeUndoableEdit(paragraph, sCopy, replace))
				if (replace) attr.removeAttributes(attr)
				attr.addAttributes(s)
			}
			changes.end()
			fireChangedUpdate(changes)
			fireUndoableEditUpdate(UndoableEditEvent(GlobalSettings, changes))
		} finally {
			writeUnlock()
		}
	}

	init {
		mainMenu(menuBar, frame)
		val ktDocument = KtDocument()
		editor.document = ktDocument
		document = ktDocument.createHandler()
		FileDrop(mainPanel) {
			it.firstOrNull { it.canRead() }?.let(::loadFile)
		}
	}

	/**
	 * Should only be called once, extracted from the constructor
	 * to shorten the startup time
	 */
	@JvmName("   ")
	internal fun postInit() {
		init()
		val lastOpenedFile = File(GlobalSettings.lastOpenedFile)
		if (lastOpenedFile.canRead()) {
			edited = false
			loadFile(lastOpenedFile)
		}
	}

	fun createNewFile(templateName: String) {
		if (!makeSureLeaveCurrentFile()) {
			currentFile = null
			edited = true
			document.clear()
			document.insert(0, javaClass
					.getResourceAsStream("/template/$templateName")
					.reader()
					.readText())
		}
	}

	override fun loadFile(it: File) {
		if (it.canRead() and !makeSureLeaveCurrentFile()) {
			currentFile = it
			message("Loaded ${it.absolutePath}")
			val path = it.absolutePath.orEmpty()
			document.switchLanguage(it.name)
			document.clear()
			document.insert(0, it.readText())
			edited = false
			GlobalSettings.lastOpenedFile = path
			GlobalSettings.recentFiles.add(it)
		}
		updateShowInFilesMenuItem()
	}

	//Shortcuts ↓↓↓
	fun undo() {
		if (undoManager.canUndo()) {
			message("Undo!")
			undoManager.undo()
			edited = true
		}
	}

	fun redo() {
		if (undoManager.canRedo()) {
			message("Redo!")
			undoManager.redo()
			edited = true
		}
	}

	fun selectAll() {
		message("Select All")
		editor.selectAll()
	}

	fun cut() {
		message("Cut selection")
		editor.cut()
	}

	fun copy() {
		message("Copied selection")
		editor.copy()
	}

	fun paste() {
		message("Pasted to current position")
		editor.paste()
	}

	fun gotoLine() {
		GoToLineDialog(this@UIImpl, document.document).show
	}

	fun find() {
		FindDialog(this@UIImpl, document).show
	}

	fun replace() {
		ReplaceDialog(this@UIImpl, document).show
	}

	fun save() {
		val file = currentFile ?: JFileChooser(GlobalSettings.recentFiles.firstOrNull()?.parentFile).apply {
			showSaveDialog(mainPanel)
			fileSelectionMode = JFileChooser.FILES_ONLY
		}.selectedFile ?: return
		currentFile = file
		if (!file.exists()) file.createNewFile()
		GlobalSettings.recentFiles.add(file)
		file.writeText(editor.text) // here, it is better to use `editor.text` instead of `document.text`
		message("Saved to ${file.absolutePath}")
		edited = false
	}

	fun nextLine() = document.nextLine()
	fun splitLine() = document.splitLine()
	fun newLineBeforeCurrent() = document.newLineBeforeCurrent()

	fun commentCurrent() {
		val start = document.lineOf(editor.selectionStart)
		val end = document.lineOf(editor.selectionEnd)
		comment(start..end)
	}

	fun comment(lines: IntRange) {
		val lineCommentStart = document.lineCommentStart
		val add = lines.any {
			val lineStart = document.startOffsetOf(it)
			val lineEnd = document.endOffsetOf(it)
			val lineText = document.textWithin(lineStart, lineEnd)
			!lineText.startsWith(lineCommentStart)        //只要有一行开头不为 `//` 就进行添加注释操作
		}
		//这上面和下面感觉可以优化emmmm
		lines.forEach {
			val lineStart = document.startOffsetOf(it)
			if (add) document.insert(lineStart, lineCommentStart)
			else document.delete(lineStart, lineCommentStart.length)
		}
	}

	//Shortcuts ↑↑↑

	override fun psiFile() = document.psiFile

	override fun makeSureLeaveCurrentFile() =
			edited && super.makeSureLeaveCurrentFile()

	override fun updateShowInFilesMenuItem() {
		showInFilesMenuItem.isEnabled = currentFile != null
		buildMenuBar.isVisible = document.psiFile is KtFile
		// saveMenuItem.isEnabled = currentFileNotNull
	}

	/**
	 * Just to reuse some codes in [reloadSettings] and [postInit]
	 */
	private fun init() {
		refreshLineNumber()
		memoryIndicator.font = messageLabel.font.run { deriveFont(size2D - 2.5F) }
	}

	override fun reloadSettings() {
		frame.bounds = GlobalSettings.windowBounds
		imageCache = null
		loadFont()
		refreshTitle()
		init()
		with(document) {
			adjustFormat()
			reparse()
		}
	}

	override fun refreshTitle() {
		frame.title = buildString {
			if (edited) append("*")
			append(currentFile?.absolutePath ?: "Untitled")
			append(" - ")
			append(GlobalSettings.appName)
		}
	}

	var editorFont: Font
		set(value) {
			editor.font = value
		}
		get() = editor.font
}
