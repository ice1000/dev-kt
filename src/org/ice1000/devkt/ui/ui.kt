package org.ice1000.devkt.ui

import com.bulenkov.iconloader.util.SystemInfo
import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.handleException
import org.ice1000.devkt.selfLocation
import org.ice1000.devkt.ui.swing.DevKtFrame
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.tryConstructClassFromStringArgs
import java.io.File
import kotlin.concurrent.thread

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
abstract class UIBase<TextAttributes> {
	var currentFile: File? = null
		set(value) {
			val change = field != value
			field = value
			if (change) {
				refreshTitle()
				updateShowInFilesMenuItem()
			}
		}
	protected abstract val document: DevKtDocumentHandler<TextAttributes>
	protected abstract var edited: Boolean

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
	protected abstract fun reloadSettings()
	protected abstract fun doBrowse(url: String)
	protected abstract fun doOpen(file: File)
	abstract fun dialogYesNo(
			text: String,
			messageType: MessageType,
			title: String = messageType.name): Boolean

	abstract fun dialog(
			text: String,
			messageType: MessageType,
			title: String = messageType.name)

	open fun makeSureLeaveCurrentFile() = dialogYesNo(
			"${currentFile?.name ?: "Current file"} unsaved, leave?",
			MessageType.Question)

	fun browse(url: String) = try {
		doBrowse(url)
		message("Browsing $url")
	} catch (e: Exception) {
		dialog("Error when browsing $url:\n${e.message}", MessageType.Error)
		message("Failed to browse $url")
	}

	fun sync() {
		currentFile?.let(::loadFile)
	}

	fun showInFiles() {
		currentFile?.run { open(parentFile) }
	}

	fun exit() {
		GlobalSettings.save()
		if (!makeSureLeaveCurrentFile()) {
			dispose()
			System.exit(0)
		}
	}

	fun restart() {
		reloadSettings()
		dispose()
		DevKtFrame()
	}

	fun open(file: File) = try {
		doOpen(file)
		message("Opened $file")
	} catch (e: Exception) {
		dialog("Error when opening ${file.absolutePath}:\n${e.message}", MessageType.Error)
		message("Failed to open $file")
	}

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
				dialog("Unsupported OS!", MessageType.Error)
				return
			}
		}
		currentFile?.run { processBuilder.directory(parentFile.absoluteFile) }
		processBuilder.start()
	}

	inline fun buildAsJar(crossinline callback: (Boolean) -> Unit = { }) = thread {
		val start = System.currentTimeMillis()
		val ktFile = psiFile() as? KtFile ?: return@thread
		try {
			message("Build started…")
			Analyzer.compileJar(ktFile)
			uiThread {
				message("Build finished in ${System.currentTimeMillis() - start}ms.")
				callback(true)
			}
		} catch (e: Exception) {
			uiThread {
				message("Build failed in ${System.currentTimeMillis() - start}ms.")
				dialog("Build failed: ${e.message}",
						MessageType.Error,
						"Build As Jar")
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
			uiThread {
				message("Build finished in ${System.currentTimeMillis() - start}ms.")
				callback(true)
			}
		} catch (e: Exception) {
			uiThread {
				message("Build failed in ${System.currentTimeMillis() - start}ms.")
				dialog("Build failed: ${e.message}",
						MessageType.Error,
						"Build As JS")
				callback(false)
			}
		}
	}

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

	fun buildJarAndRun() {
		buildAsJar { if (it) runCommand(Analyzer.targetJar) }
	}

	fun runScript() {
		val currentFile = currentFile ?: return
		val `class` = currentFile.let(Analyzer::compileScript) ?: return
		handleException {
			tryConstructClassFromStringArgs(`class`, listOf(currentFile.absolutePath))
		}
	}

	protected abstract fun dispose()
}
