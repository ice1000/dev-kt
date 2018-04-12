package org.ice1000.devkt.lang

import org.ice1000.devkt.config.ColorScheme
import org.ice1000.devkt.stringTemplateTokens
import org.ice1000.devkt.stringTokens
import org.jetbrains.kotlin.com.intellij.lexer.JavaDocTokenTypes
import org.jetbrains.kotlin.com.intellij.psi.JavaDocTokenType
import org.jetbrains.kotlin.com.intellij.psi.JavaTokenType
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens

/**
 * @author ice1000
 * @since v1.2
 * @see com.intellij.openapi.fileTypes.SyntaxHighlighter
 */
interface SyntaxHighlighter<TextAttributes> {
	/**
	 * @see com.intellij.openapi.fileTypes.SyntaxHighlighter.getTokenHighlights
	 */
	fun attributesOf(type: IElementType, colorScheme: ColorScheme<TextAttributes>): TextAttributes?
}

class KotlinSyntaxHighlighter<TextAttributes> : SyntaxHighlighter<TextAttributes> {
	override fun attributesOf(type: IElementType, colorScheme: ColorScheme<TextAttributes>) = when (type) {
		KtTokens.IDENTIFIER -> colorScheme.identifiers
		KtTokens.CHARACTER_LITERAL -> colorScheme.charLiteral
		KtTokens.EOL_COMMENT -> colorScheme.lineComments
		KtTokens.DOC_COMMENT -> colorScheme.docComments
		KtTokens.SEMICOLON -> colorScheme.semicolon
		KtTokens.COLON -> colorScheme.colon
		KtTokens.COMMA -> colorScheme.comma
		KtTokens.INTEGER_LITERAL, KtTokens.FLOAT_LITERAL -> colorScheme.numbers
		KtTokens.LPAR, KtTokens.RPAR -> colorScheme.parentheses
		KtTokens.LBRACE, KtTokens.RBRACE -> colorScheme.braces
		KtTokens.LBRACKET, KtTokens.RBRACKET -> colorScheme.brackets
		KtTokens.BLOCK_COMMENT, KtTokens.SHEBANG_COMMENT -> colorScheme.blockComments
		in stringTokens -> colorScheme.string
		in stringTemplateTokens -> colorScheme.templateEntries
		in KtTokens.KEYWORDS -> colorScheme.keywords
		in KtTokens.OPERATIONS -> colorScheme.operators
		else -> null
	}
}

class JavaSyntaxHighlighter<TextAttributes> : SyntaxHighlighter<TextAttributes> {
	override fun attributesOf(type: IElementType, colorScheme: ColorScheme<TextAttributes>) = when (type) {
		JavaTokenType.END_OF_LINE_COMMENT -> colorScheme.lineComments
		JavaDocTokenType.ALL_JAVADOC_TOKENS -> colorScheme.docComments
		JavaTokenType.C_STYLE_COMMENT -> colorScheme.blockComments
		else -> null
	}

	companion object TokenSets {

	}
}
