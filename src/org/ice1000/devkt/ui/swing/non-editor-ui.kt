package org.ice1000.devkt.ui.swing

import charlie.gensokyo.show
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.PsiViewerImpl
import org.ice1000.devkt.ui.ChooseFileType
import org.ice1000.devkt.ui.DevKtDocument
import org.ice1000.devkt.ui.DevKtDocumentHandler
import org.ice1000.devkt.ui.MessageType
import org.ice1000.devkt.ui.swing.forms.Find
import org.ice1000.devkt.ui.swing.forms.GoToLine
import org.ice1000.devkt.ui.swing.forms.UI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URL
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

abstract class AbstractUI(protected val frame: DevKtFrame) : UI() {
	init {
		frame.jMenuBar = menuBar
		with(scrollPane) {
			viewport.isOpaque = false
			verticalScrollBar.unitIncrement = 16
		}

		memoryIndicator.addMouseListener(object : MouseAdapter() {
			override fun mouseClicked(e: MouseEvent?) {
				System.gc()
				refreshMemoryIndicator()
			}
		})
	}

	var imageCache: Image? = null
	var backgroundColorCache: Color? = null

	final override fun createUIComponents() {
		mainPanel = object : JPanel() {
			public override fun paintComponent(g: Graphics) {
				super.paintComponent(g)
				val image = GlobalSettings.backgroundImage.second
				if (null != image) g.drawImage(imageCache ?: image
						.getScaledInstance(mainPanel.width, mainPanel.height, Image.SCALE_SMOOTH)
						.also { imageCache = it }, 0, 0, null)
				g.color = backgroundColorCache ?: Color.decode(GlobalSettings.colorBackground)
						.run { Color(red, green, blue, GlobalSettings.backgroundAlpha) }
						.also { backgroundColorCache = it }
				g.fillRect(0, 0, mainPanel.width, mainPanel.height)
				refreshMemoryIndicator()
			}
		}
	}

	override var memoryIndicatorText
		get() = memoryIndicator.text
		set(value) {
			memoryIndicator.text = value
		}


	override fun message(text: String) {
		messageLabel.text = text
	}

	override fun dialog(text: String, messageType: MessageType, title: String) {
		JOptionPane.showMessageDialog(mainPanel, text, title, when (messageType) {
			MessageType.Error -> JOptionPane.ERROR_MESSAGE
			MessageType.Information -> JOptionPane.INFORMATION_MESSAGE
			MessageType.Plain -> JOptionPane.PLAIN_MESSAGE
			MessageType.Question -> JOptionPane.QUESTION_MESSAGE
			MessageType.Warning -> JOptionPane.WARNING_MESSAGE
		})
	}

	override fun dialogYesNo(text: String, messageType: MessageType, title: String) =
			JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainPanel, text, title, when (messageType) {
				MessageType.Error -> JOptionPane.ERROR_MESSAGE
				MessageType.Information -> JOptionPane.INFORMATION_MESSAGE
				MessageType.Plain -> JOptionPane.PLAIN_MESSAGE
				MessageType.Question -> JOptionPane.QUESTION_MESSAGE
				MessageType.Warning -> JOptionPane.WARNING_MESSAGE
			}, JOptionPane.YES_NO_OPTION)

	override fun chooseFile(
			from: File?, chooseFileType: ChooseFileType): File? =
			jFileChooser(from, chooseFileType).selectedFile

	override fun chooseDir(
			from: File?, chooseFileType: ChooseFileType): File? =
			jFileChooser(from, chooseFileType).currentDirectory

	private fun jFileChooser(from: File?, chooseFileType: ChooseFileType): JFileChooser {
		return JFileChooser(from).apply {
			dialogType = when (chooseFileType) {
				ChooseFileType.Open -> JFileChooser.OPEN_DIALOG
				ChooseFileType.Save,
				ChooseFileType.Create -> JFileChooser.SAVE_DIALOG
			}
			fileSelectionMode = JFileChooser.FILES_ONLY
			showDialog(mainPanel, null)
		}
	}

	override fun doBrowse(url: String) = Desktop.getDesktop().browse(URL(url).toURI())
	override fun doOpen(file: File) = Desktop.getDesktop().open(file)
	override fun uiThread(lambda: () -> Unit) = SwingUtilities.invokeLater(lambda)
	override fun dispose() = frame.dispose()
	override fun createSelf() {
		DevKtFrame()
	}

	fun settings() {
		ConfigurationImpl(this, frame).show
	}

	fun viewPsi() {
		psiFile()?.let { PsiViewerImpl(it, frame).show }
	}
}

class GoToLineDialog(uiImpl: AbstractUI, private val document: DevKtDocument<*>) : GoToLine() {
	init {
		setLocationRelativeTo(uiImpl.mainPanel)
		getRootPane().defaultButton = okButton
		contentPane = mainPanel
		title = "Go to Line/Column"
		isModal = true
		pack()
		okButton.addActionListener { ok() }
		cancelButton.addActionListener { dispose() }
		lineColumn.text = document.posToLineColumn(document.caretPosition).let { (line, column) ->
			"$line:$column"
		}
	}

	private fun ok() {
		val input = lineColumn.text.split(':')
		val line = input.firstOrNull()?.toIntOrNull() ?: return
		val column = input.getOrNull(1)?.toIntOrNull() ?: 1
		document.caretPosition = document.lineColumnToPos(line, column)
		dispose()
	}
}

data class SearchResult(val start: Int, val end: Int)

open class FindDialog(
		uiImpl: AbstractUI,
		val document: DevKtDocumentHandler<*>) : Find() {
	companion object {
		val NO_REGEXP_CHARS = arrayOf(
				'\\', '{', '[', '(', '+', '*', '^', '$', '.', '?', '|'
		)
	}

	protected open var searchResult = ArrayList<SearchResult>()
	protected open var currentIndex = 0

	init {
		setLocationRelativeTo(uiImpl.mainPanel)

		contentPane = mainPanel
		title = "Find"
		isModal = true

		pack()

		moveUp.addActionListener { moveUp() }
		moveDown.addActionListener { moveDown() }
		isMatchCase.addActionListener { search() }
		isRegex.addActionListener { search() }
		input.document.addDocumentListener(object : DocumentListener {
			override fun changedUpdate(e: DocumentEvent?) = Unit                //不懂调用条件。。。
			override fun insertUpdate(e: DocumentEvent?) = removeUpdate(e)
			override fun removeUpdate(e: DocumentEvent?) = search()
		})
	}

	final override fun setLocationRelativeTo(c: Component?) = super.setLocationRelativeTo(c)
	final override fun pack() = super.pack()

	protected open fun search() {
		searchResult.clear()
		document.selectionEnd = document.selectionStart

		val input = input.text
		val text = document.text
		val regex = if (isRegex.isSelected.not()) {                //FIXME stupid code 我太菜了
			var tempInput = input
			NO_REGEXP_CHARS.forEach {
				tempInput = tempInput.replace(it.toString(), "\\$it")
			}

			tempInput
		} else input

		try {
			Pattern.compile(
					regex,
					if (isMatchCase.isSelected.not()) Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE else 0
			).matcher(text).run {
				while (find()) {
					searchResult.add(SearchResult(start(), end()))
				}
			}

			select(0)
		} catch (e: PatternSyntaxException) {
			//TODO 做出提示
		}
	}

	protected open fun select(index: Int) {
		searchResult.getOrNull(index)?.let { (start, end) ->
			currentIndex = index
			document.selectionStart = start
			document.selectionEnd = end
		}
	}

	protected open fun moveUp() {
		select(currentIndex - 1)
	}

	protected open fun moveDown() {
		select(currentIndex + 1)
	}
}

class ReplaceDialog(
		uiImpl: AbstractUI, document: DevKtDocumentHandler<*>) :
		FindDialog(uiImpl, document) {
	init {
		title = "Replace"
		listOf<JComponent>(separator, replaceInput, replace, replaceAll).forEach {
			it.isVisible = true
		}

		pack()

		replace.addActionListener { replaceCurrent() }
		replaceAll.addActionListener { replaceAll() }
	}

	private fun replaceCurrent() {
		searchResult.getOrNull(currentIndex)?.run {
			document.resetTextTo(document.text.replaceRange(start until end, replaceInput.text))
		}
	}

	private fun replaceAll() {
		val findInput = input.text
		val replaceInput = replaceInput.text
		document.resetTextTo(if (isRegex.isSelected) {
			document.replaceText(Regex(findInput), replaceInput)
		} else document.text.replace(findInput, replaceInput))
	}
}