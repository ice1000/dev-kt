package org.ice1000.devkt.ui.swing

import charlie.gensokyo.*
import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.keyMap
import org.ice1000.devkt.lie.mac
import org.ice1000.devkt.ui.DevKtIcons
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*
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
				icon = DevKtIcons.KOTLIN_FILE
				onAction { createNewFile("file.kt") }
			}
			item("Script") {
				icon = DevKtIcons.KOTLIN_FILE
				onAction { createNewFile("script.kts") }
			}
			item("Android Activity") {
				icon = DevKtIcons.KOTLIN_ANDROID
				onAction { createNewFile("activity.kt") }
			}
			item("Analyzer Gradle File") {
				icon = DevKtIcons.GRADLE
				onAction { createNewFile("build.gradle.kts") }
			}
			item("KotlinJS File") {
				icon = DevKtIcons.KOTLIN_JS
				onAction { createNewFile("js.kt") }
			}
			item("Multiplatform (Common)") {
				icon = DevKtIcons.KOTLIN_MP
				onAction { createNewFile("mp-common.kt") }
			}
			item("Multiplatform (Implementation)") {
				icon = DevKtIcons.KOTLIN_MP
				onAction { createNewFile("mp-impl.kt") }
			}
		}
		item("Open...") {
			icon = DevKtIcons.OPEN
			onAction { open() }
			GlobalSettings.shortcutGoto.isAlt
			keyMap(GlobalSettings.shortcutOpen)
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
			icon = DevKtIcons.SETTINGS
			onAction { settings() }
		}
		item("Import Settings...") {
			onAction { importSettings() }
		}
		item("Sync Settings") {
			icon = DevKtIcons.REFRESH
			onAction { restart() }
		}
		separator
		saveMenuItem = item("Save") {
			icon = DevKtIcons.SAVE
			onAction { save() }
			keyMap(GlobalSettings.shortcutSave)
		}
		item("Sync") {
			icon = DevKtIcons.SYNCHRONIZE
			onAction { sync() }
			keyMap(GlobalSettings.shortcutSync)
		}
		separator
		if (!mac) item("Exit") {
			icon = DevKtIcons.EXIT
			onAction { exit() }
		}
	}
	menuBar.subMenu("Edit") {
		mnemonic = KeyEvent.VK_E
		undoMenuItem = item("Undo") {
			icon = DevKtIcons.UNDO
			onAction { undo() }
			keyMap(GlobalSettings.shortcutUndo)
		}
		redoMenuItem = item("Redo") {
			icon = DevKtIcons.REDO
			onAction { redo() }
			keyMap(GlobalSettings.shortcutRedo)
		}
		separator
		item("Cut") {
			icon = DevKtIcons.CUT
			onAction { cut() }
		}
		item("Copy") {
			icon = DevKtIcons.COPY
			onAction { copy() }
		}
		item("Paste") {
			icon = DevKtIcons.PASTE
			onAction { paste() }
		}
		item("Select All") {
			icon = DevKtIcons.SELECT_ALL
			onAction { selectAll() }
		}
		separator
		item("New Line") {
			onAction { nextLine() }
			keyMap(GlobalSettings.shortcutNextLine)
		}
		item("New Line Before") {
			onAction { newLineBeforeCurrent() }
			keyMap(GlobalSettings.shortcutNewLineBefore)
		}
		item("Split Line") {
			onAction { splitLine() }
			keyMap(GlobalSettings.shortcutSplitLine)
		}
		item("Go to Line") {
			keyMap(GlobalSettings.shortcutGoto)
			onAction { gotoLine() }
		}
		separator
		item("Toggle Line Comment") {
			keyMap(GlobalSettings.shortcutComment)
			onAction { commentCurrent() }
		}
		item("Insert Block Comment") {
			keyMap(GlobalSettings.shortcutBlockComment)
			onAction { blockComment() }
		}
		separator
		item("Find") {
			keyMap(GlobalSettings.shortcutFind)
			onAction { find() }
		}
		item("Replace") {
			keyMap(GlobalSettings.shortcutReplace)
			onAction { replace() }
		}
	}
	buildMenuBar = menuBar.subMenu("Build") {
		mnemonic = KeyEvent.VK_B
		subMenu("Build As") {
			icon = DevKtIcons.COMPILE
			item("Jar") {
				icon = DevKtIcons.JAR
				onAction { buildAsJar() }
			}
			item("Classes") {
				icon = DevKtIcons.CLASS
				onAction { buildAsClasses() }
			}
			item("JavaScript Module") {
				icon = DevKtIcons.KOTLIN_JS
				onAction { buildAsJs() }
			}
		}
		subMenu("Build and Run As") {
			icon = DevKtIcons.EXECUTE
			item("Jar") {
				icon = DevKtIcons.JAR
				onAction { buildJarAndRun() }
			}
			item("Classes") {
				icon = DevKtIcons.CLASS
				onAction { buildClassAndRun() }
				keyMap(GlobalSettings.shortcutBuildRunAsClass)
			}
		}
		subMenu("Run As") {
			icon = DevKtIcons.EXECUTE
			item("Kotlin Script") {
				icon = DevKtIcons.KOTLIN_FILE
				onAction {
					// TODO
					JOptionPane.showMessageDialog(frame, "This feature is TODO.",
							"Unfinished", 1, DevKtIcons.KOTLIN)
				}
			}
		}
	}
	menuBar.subMenu("Help") {
		mnemonic = KeyEvent.VK_H
		item("View Psi...") {
			icon = DevKtIcons.DUMP
			onAction { viewPsi() }
		}
		subMenu("Alternatives") {
			item("IntelliJ IDEA") {
				icon = DevKtIcons.IDEA
				onAction { idea() }
			}
			item("CLion") {
				icon = DevKtIcons.CLION

				onAction { clion() }
			}
			item("Eclipse") {
				icon = DevKtIcons.ECLIPSE
				onAction { eclipse() }
			}
			item("Emacs") {
				icon = DevKtIcons.EMACS
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
