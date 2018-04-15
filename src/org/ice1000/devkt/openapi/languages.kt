package org.ice1000.devkt.openapi

import org.ice1000.devkt.lang.DevKtLanguage
import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.project.Project


/**
 * Provides default implementations to avoid breaking plugins when adding new APIs.
 *
 * @author ice1000
 * @since v1.2
 */
abstract class ExtendedDevKtLanguage<TextAttributes>(
		language: Language,
		val parserDefinition: ParserDefinition
) : DevKtLanguage<TextAttributes>(language) {
	/**
	 * Line comment start, used when pressing <kbd>Ctrl</kbd> + <kbd>/</kbd>
	 */
	override val lineCommentStart: String? get() = null

	/**
	 * Block comment surrounding, used when pressing
	 * <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>/</kbd>
	 */
	override val blockComment: Pair<String, String>? get() = null

	/**
	 * Creates a lexer for syntax highlight
	 *
	 * @param project Project current project, mocked from Kotlin compiler
	 * @return Lexer the Lexer used for syntax highlight
	 */
	override fun createLexer(project: Project): Lexer = parserDefinition.createLexer(project)

	/**
	 * Check if a file is of this language
	 * @param fileName String the file name
	 * @return Boolean is of this language or not
	 */
	override fun satisfies(fileName: String) = false
}
