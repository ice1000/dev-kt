package org.ice1000.devkt.ui

import charlie.gensokyo.show
import org.ice1000.devkt.Kotlin
import org.ice1000.devkt.`{-# LANGUAGE DevKt #-}`
import org.ice1000.devkt.config.ConfigurationImpl
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.psi.PsiViewerImpl
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import java.awt.*
import java.io.File
import java.net.URL
import javax.swing.*
import javax.swing.text.AttributeSet

abstract class AbstractUI(protected val frame: `{-# LANGUAGE DevKt #-}`) : UI() {
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
			if (change) refreshTitle()
		}
	protected var ktFileCache: KtFile? = null

	final override fun createUIComponents() {
		mainPanel = object : JPanel() {
			public override fun paintComponent(g: Graphics) {
				super.paintComponent(g)
				val image = GlobalSettings.backgroundImage.second ?: return
				g.drawImage(imageCache ?: image
						.getScaledInstance(mainPanel.width, mainPanel.height, Image.SCALE_SMOOTH)
						.also { imageCache = it }, 0, 0, null)
				g.color = backgroundColorCache ?: editor.background
						.run { Color(red, green, blue, GlobalSettings.backgroundAlpha) }
						.also { backgroundColorCache = it }
				g.fillRect(0, 0, mainPanel.width, mainPanel.height)
			}
		}
	}

	abstract fun loadFile(it: File)

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

	fun browse(url: String) = try {
		Desktop.getDesktop().browse(URL(url).toURI())
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when browsing $url:\n${e.message}")
	}

	private fun open(file: File) = try {
		Desktop.getDesktop().open(file)
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when opening ${file.absolutePath}:\n${e.message}")
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

	fun buildAsJar() {
		frame.TODO()
	}

	fun buildAsJs() {
		Kotlin.compileJs(ktFile())
	}

	fun buildAsClasses() = try {
		Kotlin.compileJvm(ktFile())
		messageLabel.text = "Build successfully."
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(frame, "Build failed: ${e.message}", "Build As Classes", 1, AllIcons.KOTLIN)
	}

	open fun makeSureLeaveCurrentFile() = JOptionPane.YES_OPTION !=
			JOptionPane.showConfirmDialog(
					mainPanel,
					"${currentFile?.name ?: "Current file"} unsaved, leave?",
					UIManager.getString("OptionPane.titleText"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					AllIcons.KOTLIN)

	protected abstract fun ktFile(): KtFile
	abstract fun reloadSettings()
}

/**
 * @author ice1000
 */
interface AnnotationHolder {
	val text: String
	fun adjustFormat()
	fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet)

	fun highlight(range: TextRange, attributeSet: AttributeSet) =
			highlight(range.startOffset, range.endOffset, attributeSet)

	fun highlight(astNode: ASTNode, attributeSet: AttributeSet) =
			highlight(astNode.textRange, attributeSet)

	fun highlight(element: PsiElement, attributeSet: AttributeSet) =
			highlight(element.textRange, attributeSet)
}
