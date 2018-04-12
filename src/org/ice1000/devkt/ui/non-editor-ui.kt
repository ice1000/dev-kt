package org.ice1000.devkt.ui

import charlie.gensokyo.show
import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.PsiViewerImpl
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
			"Unfinished", 1, DevKtIcons.KOTLIN)
}

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
			Analyzer.compileJar(ktFile())
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
		try {
			message("Build started…")
			Analyzer.compileJs(ktFile())
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

	inline fun buildAsClasses(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		try {
			message("Build started…")
			Analyzer.compileJvm(ktFile())
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

	abstract fun ktFile(): KtFile
	protected abstract fun reloadSettings()
	fun restart() {
		reloadSettings()
		frame.dispose()
		DevKtFrame()
	}

	abstract fun updateShowInFilesMenuItem()
	fun runCommand(file: File) {
		val ktFile = ktFile()
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

/**
 * @author ice1000
 */
interface AnnotationHolder<in TextAttributes> : LengthOwner {
	val text: String
	fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: TextAttributes)

	fun highlight(range: TextRange, attributeSet: TextAttributes) =
			highlight(range.startOffset, range.endOffset, attributeSet)

	fun highlight(astNode: ASTNode, attributeSet: TextAttributes) =
			highlight(astNode.textRange, attributeSet)

	fun highlight(element: PsiElement, attributeSet: TextAttributes) =
			highlight(element.textRange, attributeSet)
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
