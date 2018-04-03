package org.ice1000.devkt.ui

import charlie.gensokyo.*
import org.ice1000.devkt.AllIcons
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.filechooser.FileFilter

/**
 * DSL that initializes [menuBar]
 * as well as [UIImpl.undoMenuItem] and [UIImpl.redoMenuItem]
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
				onAction { createNewFile("file") }
			}
			item("Script") {
				icon = AllIcons.KOTLIN_FILE
				onAction { frame.TODO() }
			}
			item("Android Activity") {
				icon = AllIcons.KOTLIN_ANDROID
				onAction { frame.TODO() }
			}
			item("KotlinJS File") {
				icon = AllIcons.KOTLIN_JS
				onAction { frame.TODO() }
			}
			item("Multiplatform (Common)") {
				icon = AllIcons.KOTLIN_MP
				onAction { frame.TODO() }
			}
			item("Multiplatform (Implementation)") {
				icon = AllIcons.KOTLIN_MP
				onAction { frame.TODO() }
			}
		}
		item("Open...") {
			icon = AllIcons.OPEN
			onAction { open() }
		}
		item("Show in Files") {
			showInFilesMenuItem = this
			onAction { showInFiles() }
		}
		subMenu("Open Recent") {
			settings.recentFiles.forEach { recent ->
				val presentableFile = currentFile?.let { current -> recent.relativeTo(current.parentFile) }
						?: recent.absoluteFile
				item(presentableFile.toString()) {
					onAction { loadFile(presentableFile) }
				}
			}
		}
		separator
		item("Settings") {
			icon = AllIcons.SETTINGS
			onAction { frame.TODO() }
		}
		separator
		item("Save") {
			icon = AllIcons.SAVE
			saveMenuItem = this
			onAction { save() }
		}
		item("Sync") {
			icon = AllIcons.SYNCHRONIZE
			onAction { sync() }
		}
		separator
		item("Exit") {
			icon = AllIcons.EXIT
			onAction { exit() }
		}
	}
	menuBar.subMenu("Edit") {
		mnemonic = KeyEvent.VK_E
		item("Undo") {
			undoMenuItem = this
			icon = AllIcons.UNDO
			onAction { undo() }
		}
		item("Redo") {
			redoMenuItem = this
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
		}
		item("Build And Run") {
			icon = AllIcons.EXECUTE
			onAction { buildAndRun() }
		}
		subMenu("Run As") {
			icon = AllIcons.EXECUTE
			item("Executable Jar") {
				icon = AllIcons.JAR
				onAction { frame.TODO() }
			}
			item("Classes") {
				icon = AllIcons.CLASS
				onAction { frame.TODO() }
			}
			item("Kotlin Script") {
				icon = AllIcons.KOTLIN_FILE
				onAction { frame.TODO() }
			}
		}
	}
	menuBar.subMenu("Help") {
		mnemonic = KeyEvent.VK_H
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
			fileName.endsWith(".kt") || fileName.endsWith(".kts")
		}
	}

	override fun getDescription() = ".kt, .kts"
}
