package org.ice1000.devkt.ui

import charlie.gensokyo.*
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.controlKey
import org.ice1000.devkt.lie.mac
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.filechooser.FileFilter

/**
 * DSL that initializes [menuBar]
 *
 * @author ice1000
 * @since v0.0.1
 */
fun UIImpl.mainMenu(menuBar: JMenuBar, frame: JFrame) {
	menuBar.subMenu("File") {
		mnemonic = KeyEvent.VK_F
		subMenu("New") {
			item("Executable File") {
				icon = AllIcons.KOTLIN_FILE
				onAction { createNewFile("file.kt") }
			}
			item("Script") {
				icon = AllIcons.KOTLIN_FILE
				onAction { createNewFile("script.kts") }
			}
			item("Android Activity") {
				icon = AllIcons.KOTLIN_ANDROID
				onAction { createNewFile("activity.kt") }
			}
			item("Kotlin Gradle File") {
				icon = AllIcons.GRADLE
				onAction { createNewFile("build.gradle.kts") }
			}
			item("KotlinJS File") {
				icon = AllIcons.KOTLIN_JS
				onAction { createNewFile("js.kt") }
			}
			item("Multiplatform (Common)") {
				icon = AllIcons.KOTLIN_MP
				onAction { createNewFile("mp-common.kt") }
			}
			item("Multiplatform (Implementation)") {
				icon = AllIcons.KOTLIN_MP
				onAction { createNewFile("mp-impl.kt") }
			}
		}
		item("Open...") {
			icon = AllIcons.OPEN
			onAction { open() }
			controlKey(KeyEvent.VK_O)
		}
		showInFilesMenuItem = item("Show in Files") {
			onAction { showInFiles() }
		}
		subMenu("Open Recent") {
			GlobalSettings.recentFiles.forEach { recent ->
				val presentableFile = currentFile?.let { current -> recent.relativeTo(current.parentFile) }
						?: recent.absoluteFile
				item(presentableFile.toString()) {
					onAction { loadFile(presentableFile) }
				}
			}
		}
		separator
		if (!mac) item("Settings...") {
			icon = AllIcons.SETTINGS
			onAction { settings() }
		}
		item("Import Settings...") {
			onAction { importSettings() }
		}
		item("Sync Settings") {
			icon = AllIcons.REFRESH
			onAction { restart() }
		}
		separator
		saveMenuItem = item("Save") {
			icon = AllIcons.SAVE
			onAction { save() }
			controlKey(KeyEvent.VK_S)
		}
		item("Sync") {
			icon = AllIcons.SYNCHRONIZE
			onAction { sync() }
		}
		separator
		if (!mac) item("Exit") {
			icon = AllIcons.EXIT
			onAction { exit() }
		}
	}
	menuBar.subMenu("Edit") {
		mnemonic = KeyEvent.VK_E
		item("Undo") {
			icon = AllIcons.UNDO
			onAction { undo() }
		}
		item("Redo") {
			icon = AllIcons.REDO
			onAction { redo() }
		}
		separator
		item("Cut") {
			icon = AllIcons.CUT
			onAction { cut() }
		}
		item("Copy") {
			icon = AllIcons.COPY
			onAction { copy() }
		}
		item("Paste") {
			icon = AllIcons.PASTE
			onAction { paste() }
		}
		item("Select All") { onAction { selectAll() } }
		separator
		item("Start New Line") { onAction { nextLine() } }
	}
	menuBar.subMenu("Build") {
		mnemonic = KeyEvent.VK_B
		subMenu("Build As") {
			icon = AllIcons.COMPILE
			item("Jar") {
				icon = AllIcons.JAR
				onAction { buildAsJar() }
			}
			item("Classes") {
				icon = AllIcons.CLASS
				onAction { buildAsClasses() }
			}
			item("JavaScript Module") {
				icon = AllIcons.KOTLIN_JS
				onAction { buildAsJs() }
			}
		}
		subMenu("Run As") {
			icon = AllIcons.EXECUTE
			item("Jar") {
				icon = AllIcons.JAR
				onAction { buildJarAndRun() }
			}
			item("Classes") {
				icon = AllIcons.CLASS
				onAction { buildClassAndRun() }
			}
			item("Kotlin Script") {
				icon = AllIcons.KOTLIN_FILE
				onAction { frame.TODO() }
			}
		}
	}
	menuBar.subMenu("Help") {
		mnemonic = KeyEvent.VK_H
		item("View Psi...") {
			icon = AllIcons.KOTLIN
			onAction { viewPsi() }
		}
		subMenu("Alternatives") {
			item("IntelliJ IDEA") {
				icon = AllIcons.IDEA
				onAction { idea() }
			}
			item("CLion") {
				icon = AllIcons.CLION
				onAction { clion() }
			}
			item("Eclipse") {
				icon = AllIcons.ECLIPSE
				onAction { eclipse() }
			}
			item("Emacs") {
				icon = AllIcons.EMACS
				onAction { emacs() }
			}
		}
	}
}

val kotlinFileFilter = object : FileFilter() {
	override fun accept(input: File?) = when {
		input == null -> false
		input.isDirectory -> true
		else -> {
			val fileName = input.path.toLowerCase()
			fileName.endsWith(".kt") or fileName.endsWith(".kts")
		}
	}

	override fun getDescription() = ".kt, .kts"
}
