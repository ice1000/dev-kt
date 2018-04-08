package org.ice1000.devkt.ui

import charlie.gensokyo.show
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.Kotlin
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.psi.PsiViewerImpl
import org.ice1000.devkt.selfLocation
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.tryConstructClassFromStringArgs
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URL
import javax.swing.*
import javax.swing.text.AttributeSet
import kotlin.concurrent.thread

fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, AllIcons.KOTLIN)
}

private const val MEGABYTE = 1024 * 1024

abstract class AbstractUI(protected val frame: DevKtFrame) : UI() {
	init {
		frame.jMenuBar = menuBar
		scrollPane.viewport.isOpaque = false
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
	protected var ktFileCache: KtFile? = null

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
			// dialogTitle = "Choose a Kotlin file"
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
		PsiViewerImpl(ktFile(), frame).show
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
		try {
			message("Build started…")
			Kotlin.compileJar(ktFile())
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
						AllIcons.KOTLIN)
				callback(false)
			}
		}
	}

	inline fun buildAsJs(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		try {
			message("Build started…")
			Kotlin.compileJs(ktFile())
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
						AllIcons.KOTLIN)
				callback(false)
			}
		}
	}

	inline fun buildAsClasses(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		try {
			message("Build started…")
			Kotlin.compileJvm(ktFile())
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
						AllIcons.KOTLIN)
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
					AllIcons.KOTLIN)

	abstract fun ktFile(): KtFile
	protected abstract fun reloadSettings()
	fun restart() {
		reloadSettings()
		frame.dispose()
		DevKtFrame()
	}

	abstract fun updateShowInFilesMenuItem()
	fun runCommand(file: File) {
		val java = "java -cp ${file.absolutePath}${File.pathSeparatorChar}$selfLocation devkt.${GlobalSettings.javaClassName}Kt"
		val processBuilder = when {
			SystemInfo.isLinux -> {
				ProcessBuilder("gnome-terminal", "-x", "sh", "-c", "$java; bash")
			}
			SystemInfo.isMac -> {
				val lajiJava = "/usr/bin/$java"
				ProcessBuilder("osascript", "-e", "tell app \"Terminal\" to do script \"$lajiJava\"")
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
		val `class` = currentFile.let(Kotlin::compileScript) ?: return
		tryConstructClassFromStringArgs(`class`, listOf(currentFile.absolutePath))
	}
}

/**
 * @author ice1000
 */
interface AnnotationHolder {
	val text: String
	val len: Int
	fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet)

	fun highlight(range: TextRange, attributeSet: AttributeSet) =
			highlight(range.startOffset, range.endOffset, attributeSet)

	fun highlight(astNode: ASTNode, attributeSet: AttributeSet) =
			highlight(astNode.textRange, attributeSet)

	fun highlight(element: PsiElement, attributeSet: AttributeSet) =
			highlight(element.textRange, attributeSet)

	fun adjustFormat(offs: Int = 0, length: Int = len - offs)
}

//FIXME: tab会被当做1个字符, 不知道有没有什么解决办法
fun JTextPane.lineColumnToPos(line: Int, column: Int = 1): Int {
	val lineStart = document.defaultRootElement.getElement(line - 1).startOffset
	return lineStart + column - 1
}

fun JTextPane.posToLineColumn(pos: Int): Pair<Int, Int> {
	val root = document.defaultRootElement
	val line = root.getElementIndex(pos)
	val column = pos - root.getElement(line - 1).startOffset
	return line + 1 to column + 1
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
