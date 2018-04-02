package org.ice1000.devkt

import charlie.gensokyo.*
import org.jetbrains.kotlin.lexer.KtTokens
import java.awt.Color
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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

	init {
		frame.jMenuBar = menuBar
		val cont = StyleContext.getDefaultStyleContext()
		val attr = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.ORANGE)
		val attrGreen = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.GREEN)
		val attrBlack = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.LIGHT_GRAY)
		editor.document = object : DefaultStyledDocument() {
			init {
				addUndoableEditListener {
					undoManager.addEdit(it.edit)
					updateUndoMenuItems()
				}
			}

			override fun insertString(offs: Int, str: String, a: AttributeSet) {
				super.insertString(offs, str, a)
				val tokens = Kotlin.lex(editor.text)
				for ((tokenStart, tokenEnd, tokenType) in tokens) when (tokenType) {
					KtTokens.OPEN_QUOTE,
					KtTokens.CLOSING_QUOTE,
					KtTokens.REGULAR_STRING_PART,
					KtTokens.CHARACTER_LITERAL -> highlight(tokenStart, tokenEnd, attrGreen)
					KtTokens.FUN_KEYWORD -> highlight(tokenStart, tokenEnd, attr)
					else -> highlight(tokenStart, tokenEnd, attrBlack)
				}
			}

			private fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: AttributeSet) {
				setCharacterAttributes(tokenStart, tokenEnd - tokenStart, attributeSet, false)
			}
		}
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
			mnemonic = KeyEvent.VK_R
			item("Build As Jar") {
				icon = AllIcons.COMPILE
				onAction { frame.TODO() }
			}
			item("Build To...") {
				onAction { frame.TODO() }
			}
			subMenu("Run As") {
				icon = AllIcons.EXECUTE
				item("Executable Jar") {
					icon = AllIcons.JAR
					onAction { frame.TODO() }
				}
				item("Kotlin Script") {
					icon = AllIcons.KOTLIN_FILE
					onAction { frame.TODO() }
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
