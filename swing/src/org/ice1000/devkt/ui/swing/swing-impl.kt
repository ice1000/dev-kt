package org.ice1000.devkt.ui.swing

import charlie.gensokyo.show
import net.iharder.dnd.FileDrop
import org.ice1000.devkt.DevKtFontManager.loadFont
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.config.swingColorScheme
import org.ice1000.devkt.lang.DevKtLanguage
import org.ice1000.devkt.ui.DevKtDocument
import org.ice1000.devkt.ui.DevKtDocumentHandler
import org.ice1000.devkt.ui.swing.dialogs.FindDialogImpl
import org.ice1000.devkt.ui.swing.dialogs.GoToLineDialog
import org.ice1000.devkt.ui.swing.dialogs.ReplaceDialogImpl
import org.jetbrains.kotlin.psi.KtFile
import java.awt.Font
import java.io.File
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.event.DocumentEvent
import javax.swing.text.AttributeSet
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.MutableAttributeSet

/**
 * @author ice1000
 * @since v0.0.1
 */
class UIImpl(frame: DevKtFrame) : AbstractUI(frame) {
	internal lateinit var undoMenuItem: JMenuItem
	internal lateinit var redoMenuItem: JMenuItem
	internal lateinit var saveMenuItem: JMenuItem
	internal lateinit var showInFilesMenuItem: JMenuItem
	internal lateinit var buildMenuBar: JMenu
	internal lateinit var pluginMenuBar: JMenu
	override val document: DevKtDocumentHandler<AttributeSet>

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
		override var edited: Boolean
			get() = this@UIImpl.edited
			set(value) {
				this@UIImpl.edited = value
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
		override fun insertString(offs: Int, str: String?, a: AttributeSet?) = document.handleInsert(offs, str)

		override fun resetLineNumberLabel(str: String) {
			lineNumberLabel.text = str
		}

		override fun onChangeLanguage(newLanguage: DevKtLanguage<AttributeSet>) {
			pluginMenuBar.text = newLanguage.language.displayName
			pluginMenuBar.icon = newLanguage.icon
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
		} finally {
			writeUnlock()
		}
	}

	init {
		val ktDocument = KtDocument()
		editor.document = ktDocument
		document = ktDocument.createHandler()
		mainMenu(menuBar)
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

	fun gotoLine() = GoToLineDialog(this@UIImpl, document.document).show
	fun find() = FindDialogImpl(this@UIImpl, document).show
	fun replace() = ReplaceDialogImpl(this@UIImpl, document).show
	override fun editorText() = editor.text.orEmpty()

	override fun updateShowInFilesMenuItem() {
		showInFilesMenuItem.isEnabled = currentFile != null
		buildMenuBar.isVisible = document.psiFile is KtFile
		// saveMenuItem.isEnabled = currentFileNotNull
	}

	override fun updateUndoRedoMenuItem() {
		// undoMenuItem.isEnabled = document.canUndo
		// redoMenuItem.isEnabled = document.canRedo
	}

	/**
	 * Just to reuse some codes in [reloadSettings] and [postInit]
	 */
	private fun init() {
		with(lineNumberLabel) {
			font = editor.font
			background = editor.background.brighter()
		}
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
		frame.title = regenerateTitle()
	}

	var editorFont: Font
		set(value) {
			lineNumberLabel.font = value
			editor.font = value
		}
		get() = editor.font
}
