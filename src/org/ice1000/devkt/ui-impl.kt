package org.ice1000.devkt

import charlie.gensokyo.*
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtTokens
import java.awt.Desktop
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.net.URI
import java.net.URL
import javax.swing.*
import javax.swing.text.*
import javax.swing.undo.UndoManager

fun JFrame.TODO() {
	JOptionPane.showMessageDialog(this, "This feature is TODO.",
			"Unfinished", 1, AllIcons.KOTLIN)
}

/**
 * @author ice1000
 * @since v0.0.1
 */
class UIImpl(private val frame: `{-# LANGUAGE DevKt #-}`) : UI() {
	private val undoManager = UndoManager()
	private lateinit var undoMenuItem: JMenuItem
	private lateinit var redoMenuItem: JMenuItem

	private inner class KtDocument : DefaultStyledDocument() {
		init {
			addUndoableEditListener {
				undoManager.addEdit(it.edit)
				updateUndoMenuItems()
			}
		}

		private val stringTokens = TokenSet.create(
				KtTokens.OPEN_QUOTE,
				KtTokens.CLOSING_QUOTE,
				KtTokens.REGULAR_STRING_PART,
				KtTokens.CHARACTER_LITERAL)

		override fun insertString(offs: Int, str: String, a: AttributeSet) {
			super.insertString(offs, str, a)
			val tokens = Kotlin.lex(editor.text)
			for ((start, end, text, type) in tokens) when {
				stringTokens.contains(type) -> highlight(start, end, STRING)
				KtTokens.COMMENTS.contains(type) -> highlight(start, end, COMMENTS)
				KtTokens.KEYWORDS.contains(type) -> highlight(start, end, KEYWORDS)
				else -> highlight(start, end, OTHERS)
			}
		}

		private fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet) {
			setCharacterAttributes(tokenStart, tokenEnd - tokenStart, attributeSet, false)
		}
	}

	init {
		frame.jMenuBar = menuBar
		editor.document = KtDocument()
		menuBar.subMenu("File") {
			mnemonic = KeyEvent.VK_F
			subMenu("New") {
				item("Executable File") {
					icon = AllIcons.KOTLIN_FILE
					onAction { frame.TODO() }
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
			item("Open") {
				icon = AllIcons.OPEN
				onAction { frame.TODO() }
			}
			item("Open Recent") {
				onAction { frame.TODO() }
			}
			separator
			item("Settings") {
				icon = AllIcons.SETTINGS
				onAction { frame.TODO() }
			}
			separator
			item("Save") {
				icon = AllIcons.SAVE
				onAction { frame.TODO() }
			}
			item("Sync") {
				icon = AllIcons.SYNCHRONIZE
				onAction { frame.TODO() }
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
				onAction { copy() }
				icon = AllIcons.COPY
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
		updateUndoMenuItems()
		editor.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				if (e.isControlDown && !e.isAltDown && !e.isShiftDown && e.keyCode == KeyEvent.VK_Z) undo()
				if (e.isControlDown && !e.isAltDown && e.isShiftDown && e.keyCode == KeyEvent.VK_Z) redo()
			}
		})
	}

	private fun idea() = browse("https://www.jetbrains.com/idea/download/")
	private fun eclipse() = browse("http://marketplace.eclipse.org/content/kotlin-plugin-eclipse")
	private fun emacs() = browse("https://melpa.org/#/kotlin-mode")

	private fun browse(url: String) = try {
		Desktop.getDesktop().browse(URL(url).toURI())
	} catch (e: Exception) {
		JOptionPane.showMessageDialog(mainPanel, "Error when browsing $url:\n${e.message}")
	}

	private fun buildAsClasses() {
		Kotlin.parse(editor.text)?.let(Kotlin::compile)
	}

	private fun buildAndRun() {
		buildAsClasses()
		justRun()
		frame.TODO()
	}

	private fun justRun() {
		frame.TODO()
	}

	private fun buildAsJar() {
		frame.TODO()
	}

	private fun exit() {
		frame.dispose()
		frame.globalSettings.save()
		// TODO check saving
		System.exit(0)
	}

	private fun updateUndoMenuItems() {
		undoMenuItem.isEnabled = undoManager.canUndo()
		redoMenuItem.isEnabled = undoManager.canRedo()
	}

	private fun undo() {
		if (undoManager.canUndo()) undoManager.undo()
	}

	private fun redo() {
		if (undoManager.canRedo()) undoManager.redo()
	}

	private fun selectAll() = editor.selectAll()
	private fun cut() = editor.cut()
	private fun copy() = editor.copy()
	private fun paste() = editor.paste()
}
