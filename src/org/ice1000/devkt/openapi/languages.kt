package org.ice1000.devkt.openapi

import org.ice1000.devkt.lang.DevKtLanguage
import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.psi.TokenType
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType


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
	 * Creates a lexer for syntax highlight
	 *
	 * @param project Project current project, mocked from Kotlin compiler
	 * @return Lexer the Lexer used for syntax highlight
	 */
	override fun createLexer(project: Project): Lexer = parserDefinition.createLexer(project)

	/**
	 * @see [org.ice1000.devkt.openapi.SyntaxHighlighter.attributesOf]
	 */
	override fun attributesOf(type: IElementType, colorScheme: ColorScheme<TextAttributes>) = when (type) {
		TokenType.BAD_CHARACTER, TokenType.ERROR_ELEMENT -> colorScheme.error
		else -> null
	}
}
