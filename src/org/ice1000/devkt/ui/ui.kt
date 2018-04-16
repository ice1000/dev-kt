package org.ice1000.devkt.ui

import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import java.io.File
import javax.swing.text.AttributeSet

/**
 * @author ice1000
 * @since v1.3
 */
enum class MessageType {
	Error, Information, Plain, Question, Warning
}

/**
 * @author ice1000
 * @since v1.3
 */
abstract class UIBase {
	var currentFile: File? = null
		set(value) {
			val change = field != value
			field = value
			if (change) {
				refreshTitle()
				updateShowInFilesMenuItem()
			}
		}
	protected abstract val document: DevKtDocumentHandler<AttributeSet>

	fun idea() = browse("https://www.jetbrains.com/idea/download/")
	fun clion() = browse("https://www.jetbrains.com/clion/download/")
	fun eclipse() = browse("http://marketplace.eclipse.org/content/kotlin-plugin-eclipse")
	fun emacs() = browse("https://melpa.org/#/kotlin-mode")

	fun psiFile(): PsiFile? = document.psiFile
	abstract fun loadFile(it: File)
	abstract fun refreshTitle()
	abstract fun updateShowInFilesMenuItem()
	abstract fun uiThread(lambda: () -> Unit)
	abstract fun message(text: String)
	protected abstract fun browse(url: String)
	protected abstract fun open(file: File)
	abstract fun dialog(
			text: String,
			messageType: MessageType,
			title: String = messageType.name)
}
