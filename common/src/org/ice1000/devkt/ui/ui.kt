package org.ice1000.devkt.ui

import org.ice1000.devkt.Analyzer
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.openapi.ui.DevKtWindow
import org.ice1000.devkt.openapi.util.CompletionElement
import org.ice1000.devkt.openapi.util.selfLocation
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import javax.script.ScriptException
import kotlin.concurrent.thread

private const val MEGABYTE = 1024 * 1024

/**
 * Platform independent
 * @author ice1000
 * @since v1.3
 */
abstract class UIBase<TextAttributes> : DevKtWindow {
	override var edited = false
		set(value) {
			val change = field != value
			field = value
			if (change) {
				refreshTitle()
				updateUndoRedoMenuItem()
			}
		}

	var currentFile: File? = null
		set(value) {
			val change = field != value
			field = value
			if (change) {
				refreshTitle()
				updateShowInFilesMenuItem()
				updateUndoRedoMenuItem()
			}
		}
	abstract val document: DevKtDocumentHandler<TextAttributes>
	abstract var memoryIndicatorText: String?

	fun idea() = browse("https://www.jetbrains.com/idea/download/")
	fun clion() = browse("https://www.jetbrains.com/clion/download/")
	fun eclipse() = browse("http://marketplace.eclipse.org/content/kotlin-plugin-eclipse")
	fun emacs() = browse("https://melpa.org/#/kotlin-mode")
	fun viewSource() = browse("https://github.com/ice1000/dev-kt")
	fun createIssue() = browse("https://github.com/ice1000/dev-kt/issues/new")
	fun psiFile(): PsiFile? = document.psiFile
	fun nextLine() = document.nextLine()
	fun splitLine() = document.splitLine()
	fun newLineBeforeCurrent() = document.newLineBeforeCurrent()

	abstract fun updateShowInFilesMenuItem()
	abstract fun updateUndoRedoMenuItem()
	protected abstract fun reloadSettings()
	protected abstract fun doBrowse(url: String)
	protected abstract fun doOpen(file: File)
	protected abstract fun dispose()
	protected abstract fun createSelf()
	protected abstract fun editorText(): String

	fun makeSureLeaveCurrentFile() = edited && !dialogYesNo(
			"${currentFile?.name ?: "Current file"} unsaved, leave?",
			MessageType.Question)

	fun regenerateTitle() = buildString {
		if (edited) append("*")
		append(currentFile?.absolutePath ?: "Untitled")
		append(" - ")
		append(GlobalSettings.appName)
	}

	fun open() {
		chooseFile(currentFile?.parentFile, ChooseFileType.Open)?.let(::loadFile)
	}

	fun undo() {
		if (document.canUndo) {
			message("Undo!")
			document.undo()
			edited = true
		}
		updateUndoRedoMenuItem()
	}

	fun redo() {
		if (document.canRedo) {
			message("Redo!")
			document.redo()
			edited = true
		}
		updateUndoRedoMenuItem()
	}

	fun save() {
		val file = currentFile ?: chooseFile(GlobalSettings.recentFiles.firstOrNull()?.parentFile, ChooseFileType.Save)
		?: return
		currentFile = file
		if (!file.exists()) file.createNewFile()
		GlobalSettings.recentFiles.add(file)
		file.writeText(editorText()) // here, it is better to use `editor.text` instead of `document.text`
		message("Saved to ${file.absolutePath}")
		edited = false
	}

	fun commentCurrent() = document.commentCurrent()
	fun blockComment() = document.blockComment()

	fun refreshMemoryIndicator() {
		val runtime = Runtime.getRuntime()
		val total = runtime.totalMemory() / MEGABYTE
		val free = runtime.freeMemory() / MEGABYTE
		val used = total - free
		memoryIndicatorText = "$used of ${total}M"
	}

	override fun loadFile(it: File) {
		if (it.canRead() and !makeSureLeaveCurrentFile()) {
			currentFile = it
			message("Loaded ${it.absolutePath}")
			val path = it.absolutePath.orEmpty()
			document.switchLanguage(it.name)
			document.resetTextTo(it.readText().filterNot { it == '\r' })
			document.clearUndo()
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

	override fun sync() {
		currentFile?.let(::loadFile)
	}

	fun showInFiles() {
		currentFile?.run { open(parentFile) }
	}

	override fun exit() {
		GlobalSettings.save()
		if (!makeSureLeaveCurrentFile()) {
			dispose()
			System.exit(0)
		}
	}

	override fun restart() {
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

	fun runScript() = try {
		Analyzer.runScript(document.text)
	} catch (e: ScriptException) {
		message("Failed to run script.")
		dialog("Failed to run: ${e.message}",
				MessageType.Error,
				"Run as script")
	}
}
