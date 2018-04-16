package org.ice1000.devkt.ui

import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.handleException
import org.ice1000.devkt.selfLocation
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
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
enum class ChooseFileType {
	Open, Save, Create
}

private const val MEGABYTE = 1024 * 1024

/**
 * Platform independent
 * @author ice1000
 * @since v1.3
 */
abstract class UIBase<TextAttributes> {
	var edited = false
		set(value) {
			val change = field != value
			field = value
			if (change) refreshTitle()
		}

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
	abstract var memoryIndicatorText: String?

	fun idea() = browse("https://www.jetbrains.com/idea/download/")
	fun clion() = browse("https://www.jetbrains.com/clion/download/")
	fun eclipse() = browse("http://marketplace.eclipse.org/content/kotlin-plugin-eclipse")
	fun emacs() = browse("https://melpa.org/#/kotlin-mode")

	fun psiFile(): PsiFile? = document.psiFile
	abstract fun refreshTitle()
	abstract fun updateShowInFilesMenuItem()
	abstract fun uiThread(lambda: () -> Unit)
	abstract fun message(text: String)
	protected abstract fun reloadSettings()
	protected abstract fun doBrowse(url: String)
	protected abstract fun doOpen(file: File)
	protected abstract fun dispose()
	protected abstract fun createSelf()
	abstract fun chooseFile(from: File?, chooseFileType: ChooseFileType): File?
	abstract fun chooseDir(from: File?, chooseFileType: ChooseFileType): File?
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

	fun open() {
		chooseFile(currentFile?.parentFile, ChooseFileType.Open)?.let {
			loadFile(it)
		}
	}

	fun refreshMemoryIndicator() {
		val runtime = Runtime.getRuntime()
		val total = runtime.totalMemory() / MEGABYTE
		val free = runtime.freeMemory() / MEGABYTE
		val used = total - free
		memoryIndicatorText = "$used of ${total}M"
	}

	fun loadFile(it: File) {
		if (it.canRead() and !makeSureLeaveCurrentFile()) {
			currentFile = it
			message("Loaded ${it.absolutePath}")
			val path = it.absolutePath.orEmpty()
			document.switchLanguage(it.name)
			document.resetTextTo(it.readText().filterNot { it == '\r' })
			edited = false
			GlobalSettings.lastOpenedFile = path
			GlobalSettings.recentFiles.add(it)
		}
		updateShowInFilesMenuItem()
	}

	fun createNewFile(templateName: String) {
		if (!makeSureLeaveCurrentFile()) {
			currentFile = null
			edited = true
			document.resetTextTo(javaClass
					.getResourceAsStream("/template/$templateName")
					.reader()
					.readText())
		}
	}

	fun importSettings() {
		val file = chooseFile(File(selfLocation), ChooseFileType.Open) ?: return
		GlobalSettings.loadFile(file)
		restart()
	}

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
		createSelf()
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
}
