package org.ice1000.devkt.ui.swing

import charlie.gensokyo.show
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.PsiViewerImpl
import org.ice1000.devkt.selfLocation
import org.ice1000.devkt.ui.*
import org.ice1000.devkt.ui.swing.forms.*
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.tryConstructClassFromStringArgs
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
import kotlin.concurrent.thread

private const val MEGABYTE = 1024 * 1024

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

	fun refreshMemoryIndicator() {
		val runtime = Runtime.getRuntime()
		val total = runtime.totalMemory() / MEGABYTE
		val free = runtime.freeMemory() / MEGABYTE
		val used = total - free
		memoryIndicator.text = "$used of ${total}M"
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

	fun open() {
		JFileChooser(currentFile?.parentFile).apply {
			// dialogTitle = "Choose a Analyzer file"
			fileFilter = kotlinFileFilter
			showOpenDialog(mainPanel)
			fileSelectionMode = JFileChooser.FILES_ONLY
		}.selectedFile?.let {
			loadFile(it)
		}
	}

	fun sync() {
		currentFile?.let(::loadFile)
	}

	fun refreshLineNumber() = with(lineNumberLabel) {
		font = editor.font
		background = editor.background.brighter()
	}

	override fun browse(url: String) = try {
		Desktop.getDesktop().browse(URL(url).toURI())
		message("Browsing $url")
	} catch (e: Exception) {
		dialog("Error when browsing $url:\n${e.message}", MessageType.Error)
		message("Failed to browse $url")
	}

	override fun open(file: File) = try {
		Desktop.getDesktop().open(file)
		message("Opened $file")
	} catch (e: Exception) {
		dialog("Error when opening ${file.absolutePath}:\n${e.message}", MessageType.Error)
		message("Failed to open $file")
	}

	fun showInFiles() {
		currentFile?.run { open(parentFile) }
	}

	fun settings() {
		ConfigurationImpl(this, frame).show
	}

	fun viewPsi() {
		psiFile()?.let { PsiViewerImpl(it, frame).show }
	}

	fun exit() {
		GlobalSettings.save()
		if (!makeSureLeaveCurrentFile()) {
			frame.dispose()
			System.exit(0)
		}
	}

	fun importSettings() {
		val file = JFileChooser(selfLocation).apply {
			showOpenDialog(mainPanel)
		}.selectedFile ?: return
		GlobalSettings.loadFile(file)
		restart()
	}

	override fun uiThread(lambda: () -> Unit) = SwingUtilities.invokeLater(lambda)

	fun buildClassAndRun() {
		buildAsClasses { if (it) runCommand(Analyzer.targetDir) }
	}

	inline fun buildAsClasses(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		val ktFile = psiFile() as? KtFile ?: return@thread
		try {
			message("Build started…")
			Analyzer.compileJvm(ktFile)
			uiThread {
				message("Build finished in ${System.currentTimeMillis() - start}ms.")
				callback(true)
			}
		} catch (e: Exception) {
			uiThread {
				message("Build failed in ${System.currentTimeMillis() - start}ms.")
				dialog("Build failed: ${e.message}",
						MessageType.Error,
						"Build As Classes")
				callback(false)
			}
		}
	}

	open fun makeSureLeaveCurrentFile() = JOptionPane.YES_OPTION !=
			JOptionPane.showConfirmDialog(
					mainPanel,
					"${currentFile?.name ?: "Current file"} unsaved, leave?",
					UIManager.getString("OptionPane.titleText"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					DevKtIcons.KOTLIN)

	protected abstract fun reloadSettings()
	fun restart() {
		reloadSettings()
		frame.dispose()
		DevKtFrame()
	}

	// TODO 错误处理
	fun runScript() {
		val currentFile = currentFile ?: return
		val `class` = currentFile.let(Analyzer::compileScript) ?: return
		tryConstructClassFromStringArgs(`class`, listOf(currentFile.absolutePath))
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