package org.ice1000.devkt.ui.swing

import charlie.gensokyo.show
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.*
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.PsiViewerImpl
import org.ice1000.devkt.ui.DevKtIcons
import org.ice1000.devkt.ui.swing.forms.Find
import org.ice1000.devkt.ui.swing.forms.GoToLine
import org.ice1000.devkt.ui.swing.forms.UI
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
	var currentFile: File? = null
		set(value) {
			val change = field != value
			field = value
			if (change) {
				refreshTitle()
				updateShowInFilesMenuItem()
			}
		}

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

	abstract fun loadFile(it: File)

	fun message(text: String) {
		messageLabel.text = text
	}

	fun open() {
		JFileChooser(currentFile?.parentFile).apply {
			// dialogTitle = "Choose a Analyzer file"
			fileFilter = kotlinFileFilter
			showOpenDialog(mainPanel)
			fileSelectionMode = JFileChooser.FILES_ONLY
		}.selectedFile?.let {
			loadFile(it)
			GlobalSettings.recentFiles.add(it)
		}
	}

	fun sync() {
		currentFile?.let(::loadFile)
	}

	fun refreshLineNumber() = with(lineNumberLabel) {
		font = editor.font
		background = editor.background.brighter()
	}

	private fun browse(url: String) = try {
		Desktop.getDesktop().browse(URL(url).toURI())
		message("Browsing $url")
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when browsing $url:\n${e.message}")
		message("Failed to browse $url")
	}

	private fun open(file: File) = try {
		Desktop.getDesktop().open(file)
		message("Opened $file")
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when opening ${file.absolutePath}:\n${e.message}")
		message("Failed to open $file")
	}

	fun showInFiles() {
		currentFile?.run { open(parentFile) }
	}

	abstract fun refreshTitle()

	fun idea() = browse("https://www.jetbrains.com/idea/download/")
	fun clion() = browse("https://www.jetbrains.com/clion/download/")
	fun eclipse() = browse("http://marketplace.eclipse.org/content/kotlin-plugin-eclipse")
	fun emacs() = browse("https://melpa.org/#/kotlin-mode")
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

	inline fun buildAsJar(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		val ktFile = psiFile() as? KtFile ?: return@thread
		try {
			message("Build started…")
			Analyzer.compileJar(ktFile)
			SwingUtilities.invokeLater {
				message("Build finished in ${System.currentTimeMillis() - start}ms.")
				callback(true)
			}
		} catch (e: Exception) {
			SwingUtilities.invokeLater {
				message("Build failed in ${System.currentTimeMillis() - start}ms.")
				JOptionPane.showMessageDialog(
						mainPanel,
						"Build failed: ${e.message}",
						"Build As Jar",
						JOptionPane.ERROR_MESSAGE,
						DevKtIcons.KOTLIN)
				callback(false)
			}
		}
	}

	inline fun buildAsJs(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		val ktFile = psiFile() as? KtFile ?: return@thread
		try {
			message("Build started…")
			Analyzer.compileJs(ktFile)
			SwingUtilities.invokeLater {
				message("Build finished in ${System.currentTimeMillis() - start}ms.")
				callback(true)
			}
		} catch (e: Exception) {
			SwingUtilities.invokeLater {
				message("Build failed in ${System.currentTimeMillis() - start}ms.")
				JOptionPane.showMessageDialog(
						mainPanel,
						"Build failed: ${e.message}",
						"Build As JS",
						JOptionPane.ERROR_MESSAGE,
						DevKtIcons.KOTLIN)
				callback(false)
			}
		}
	}

	fun buildClassAndRun() {
		buildAsClasses { if (it) runCommand(Analyzer.targetDir) }
	}

	fun buildJarAndRun() {
		buildAsJar { if (it) runCommand(Analyzer.targetJar) }
	}

	inline fun buildAsClasses(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		val ktFile = psiFile() as? KtFile ?: return@thread
		try {
			message("Build started…")
			Analyzer.compileJvm(ktFile)
			SwingUtilities.invokeLater {
				message("Build finished in ${System.currentTimeMillis() - start}ms.")
				callback(true)
			}
		} catch (e: Exception) {
			SwingUtilities.invokeLater {
				message("Build failed in ${System.currentTimeMillis() - start}ms.")
				JOptionPane.showMessageDialog(
						mainPanel,
						"Build failed: ${e.message}",
						"Build As Classes",
						JOptionPane.ERROR_MESSAGE,
						DevKtIcons.KOTLIN)
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

	abstract fun psiFile(): PsiFile?
	protected abstract fun reloadSettings()
	fun restart() {
		reloadSettings()
		frame.dispose()
		DevKtFrame()
	}

	abstract fun updateShowInFilesMenuItem()
	fun runCommand(file: File) {
		val ktFile = psiFile() as? KtFile ?: return
		val className = ktFile.packageFqName.asString().let {
			if (it.isEmpty()) "${GlobalSettings.javaClassName}Kt" else "$it.${GlobalSettings.javaClassName}Kt"
		}
		val java = "java -cp ${file.absolutePath}${File.pathSeparatorChar}$selfLocation $className"
		val processBuilder = when {
			SystemInfo.isLinux -> {
				ProcessBuilder("gnome-terminal", "-x", "sh", "-c", "$java; bash")
			}
			SystemInfo.isMac -> {
				val trashJava = "/usr/bin/${java.replaceFirst(" devkt.", " ")}"// Why Analyzer has no String.replaceLast
				ProcessBuilder("osascript", "-e", "tell app \"Terminal\" to do script \"$trashJava\"")
			}
			SystemInfo.isWindows -> {
				ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", java)
			}
			else -> {
				JOptionPane.showMessageDialog(mainPanel, "Unsupported OS!")
				return
			}
		}
		currentFile?.run { processBuilder.directory(parentFile.absoluteFile) }
		processBuilder.start()
	}

	// TODO 错误处理
	fun runScript() {
		val currentFile = currentFile ?: return
		val `class` = currentFile.let(Analyzer::compileScript) ?: return
		tryConstructClassFromStringArgs(`class`, listOf(currentFile.absolutePath))
	}
}

class GoToLineDialog(uiImpl: AbstractUI, private val editor: JTextPane) : GoToLine() {
	init {
		setLocationRelativeTo(uiImpl.mainPanel)
		getRootPane().defaultButton = okButton
		contentPane = mainPanel
		title = "Go to Line/Column"
		isModal = true
		pack()
		okButton.addActionListener { ok() }
		cancelButton.addActionListener { dispose() }
		lineColumn.text = editor.posToLineColumn(editor.caretPosition).let { (line, column) ->
			"$line:$column"
		}
	}

	private fun ok() {
		val input = lineColumn.text.split(':')
		val line = input.firstOrNull()?.toIntOrNull() ?: return
		val column = input.getOrNull(1)?.toIntOrNull() ?: 1
		editor.caretPosition = editor.lineColumnToPos(line, column)
		dispose()
	}
}

data class SearchResult(val start: Int, val end: Int)

open class FindDialog(uiImpl: AbstractUI, val editor: JTextPane) : Find() {
	companion object {
		val NO_REGEXP_CHARS = arrayOf(
				'\\', '{', '[', '(', '+', '*', '^', '$', '.'
		)
	}

	protected open var searchResult = ArrayList<SearchResult>()
	protected open var currentIndex = 0

	init {
		setLocationRelativeTo(uiImpl.mainPanel)
		pack()

		contentPane = mainPanel
		title = "Find"
		isModal = true

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

	protected open fun search() {
		searchResult.clear()
		editor.selectionEnd = editor.selectionStart

		val input = input.text
		val text = editor.document.text
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
					if (isMatchCase.isSelected) Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE else 0
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
		searchResult.getOrNull(index)?.run {
			currentIndex = index
			editor.selectionStart = this.start
			editor.selectionEnd = this.end
			pack()


		}
	}

	protected open fun moveUp() {
		select(currentIndex - 1)
	}

	protected open fun moveDown() {
		select(currentIndex + 1)
	}
}

class ReplaceDialog(uiImpl: AbstractUI, editor: JTextPane) : FindDialog(uiImpl, editor) {
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
			editor.document.text = editor.document.text.replaceRange(start until end, replaceInput.text)
		}
	}

	private fun replaceAll() {
		val text = editor.document.text
		val findInput = input.text
		val replaceInput = replaceInput.text
		editor.document.text = if (isRegex.isSelected) {
			text.replace(Regex(findInput), replaceInput)
		} else text.replace(findInput, replaceInput)
	}
}