package org.ice1000.devkt.openapi.ui

import org.ice1000.devkt.lang.DevKtLanguage
import org.ice1000.devkt.openapi.AnnotationHolder
import org.ice1000.devkt.openapi.LengthOwner
import org.ice1000.devkt.ui.Edit

/**
 * @author ice1000
 * @since v1.3
 * @param TextAttributes
 * @property selectionStart Int
 * @property selectionEnd Int
 * @property canUndo Boolean
 * @property canRedo Boolean
 */
interface IDevKtDocumentHandler<TextAttributes> : AnnotationHolder<TextAttributes> {
	var selectionStart: Int
	var selectionEnd: Int
	val canUndo: Boolean
	val canRedo: Boolean
	fun startOffsetOf(line: Int): Int
	fun endOffsetOf(line: Int): Int
	fun lineOf(offset: Int): Int
	fun textWithin(start: Int, end: Int): String
	fun replaceText(regex: Regex, replacement: String): String
	fun undo()
	fun redo()
	fun done()
	fun clearUndo()
	fun addEdit(offset: Int, text: CharSequence, isInsert: Boolean)
	fun addEdit(edit: Edit)
	fun switchLanguage(fileName: String)
	fun switchLanguage(language: DevKtLanguage<TextAttributes>)
	fun adjustFormat(offs: Int = 0, len: Int = length - offs)
	/**
	 * Delete without checking
	 *
	 * @param offset Int see [delete]
	 * @param length Int see [delete]
	 */
	fun deleteDirectly(offset: Int, length: Int, reparse: Boolean = true)

	/**
	 * Handles user input, delete with checks and undo recording
	 *
	 * @param offs Int see [insert]
	 * @param len Int length of deletion
	 */
	fun delete(offs: Int, len: Int)

	/**
	 * Clear the editor and set the content with undo recording
	 *
	 * @param string String new content.
	 */
	fun resetTextTo(string: String)

	/**
	 * Insert without checking
	 *
	 * @param offset Int see [insert]
	 * @param string String see [insert]
	 * @param move Int how long the caret should move
	 */
	fun insertDirectly(offset: Int, string: String, move: Int = 0, reparse: Boolean = true)

	/**
	 * Handles user input, insert with checks and undo recording
	 *
	 * @param offs Int offset from the beginning of the document
	 * @param str String? text to insert
	 */
	fun insert(offs: Int, str: String?)

	fun reparse()
}

interface IDevKtDocument<TextAttributes> : LengthOwner {
	var caretPosition: Int
	var selectionStart: Int
	var selectionEnd: Int
	fun clear()
	fun delete(offs: Int, len: Int)
	fun insert(offs: Int, str: String?)
	fun changeCharacterAttributes(offset: Int, length: Int, s: TextAttributes, replace: Boolean)
	fun changeParagraphAttributes(offset: Int, length: Int, s: TextAttributes, replace: Boolean)
	fun resetLineNumberLabel(str: String)
	fun onChangeLanguage(newLanguage: DevKtLanguage<TextAttributes>)
	fun startOffsetOf(line: Int): Int
	fun endOffsetOf(line: Int): Int
	fun lineOf(offset: Int): Int
	fun lockWrite()
	fun unlockWrite()
	fun message(text: String)
}
