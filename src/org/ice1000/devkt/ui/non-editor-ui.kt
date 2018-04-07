package org.ice1000.devkt.ui

import charlie.gensokyo.show
import org.ice1000.devkt.Kotlin
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.psi.PsiViewerImpl
import org.ice1000.devkt.selfLocation
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import java.awt.*
import java.io.File
import java.net.URL
import javax.swing.*
import javax.swing.text.AttributeSet
import kotlin.concurrent.thread

fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, AllIcons.KOTLIN)
}

abstract class AbstractUI(protected val frame: DevKtFrame) : UI() {
	init {
		frame.jMenuBar = menuBar
		scrollPane.viewport.isOpaque = false
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
			}
		}
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
		restart()
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
