package org.ice1000.devkt.lang

import org.ice1000.devkt.config.ColorScheme
import org.ice1000.devkt.stringTemplateTokens
import org.ice1000.devkt.stringTokens
import org.jetbrains.kotlin.com.intellij.psi.JavaDocTokenType
import org.jetbrains.kotlin.com.intellij.psi.JavaTokenType
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.JavaDocElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.com.intellij.psi.tree.java.IJavaDocElementType
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
		JavaTokenType.IDENTIFIER -> colorScheme.identifiers
		JavaTokenType.CHARACTER_LITERAL -> colorScheme.charLiteral
		JavaTokenType.STRING_LITERAL -> colorScheme.string
		JavaTokenType.END_OF_LINE_COMMENT -> colorScheme.lineComments
		JavaTokenType.C_STYLE_COMMENT -> colorScheme.blockComments
		JavaTokenType.SEMICOLON -> colorScheme.semicolon
		JavaTokenType.COLON -> colorScheme.colon
		JavaTokenType.COMMA -> colorScheme.comma
		JavaDocElementType.DOC_COMMENT -> colorScheme.docComments
		JavaTokenType.INTEGER_LITERAL, JavaTokenType.FLOAT_LITERAL -> colorScheme.numbers
		JavaTokenType.LPARENTH, JavaTokenType.RPARENTH -> colorScheme.parentheses
		JavaTokenType.LBRACE, JavaTokenType.RBRACE -> colorScheme.braces
		JavaTokenType.LBRACKET, JavaTokenType.RBRACKET -> colorScheme.brackets
		in JAVA_OPERATORS -> colorScheme.operators
		in JAVA_KEYWORDS -> colorScheme.keywords
		else -> null
	}

	private companion object TokenSets {
		private val JAVA_OPERATORS = TokenSet.create(
				JavaTokenType.DOT,
				JavaTokenType.ASTERISK
		)
		private val JAVA_KEYWORDS = TokenSet.create(
				JavaTokenType.TRUE_KEYWORD,
				JavaTokenType.FALSE_KEYWORD,
				JavaTokenType.NULL_KEYWORD,
				JavaTokenType.ABSTRACT_KEYWORD,
				JavaTokenType.ASSERT_KEYWORD,
				JavaTokenType.BOOLEAN_KEYWORD,
				JavaTokenType.BREAK_KEYWORD,
				JavaTokenType.BYTE_KEYWORD,
				JavaTokenType.CASE_KEYWORD,
				JavaTokenType.CATCH_KEYWORD,
				JavaTokenType.CHAR_KEYWORD,
				JavaTokenType.CLASS_KEYWORD,
				JavaTokenType.CONST_KEYWORD,
				JavaTokenType.CONTINUE_KEYWORD,
				JavaTokenType.DEFAULT_KEYWORD,
				JavaTokenType.DO_KEYWORD,
				JavaTokenType.DOUBLE_KEYWORD,
				JavaTokenType.ELSE_KEYWORD,
				JavaTokenType.ENUM_KEYWORD,
				JavaTokenType.EXTENDS_KEYWORD,
				JavaTokenType.FINAL_KEYWORD,
				JavaTokenType.FINALLY_KEYWORD,
				JavaTokenType.FLOAT_KEYWORD,
				JavaTokenType.FOR_KEYWORD,
				JavaTokenType.GOTO_KEYWORD,
				JavaTokenType.IF_KEYWORD,
				JavaTokenType.IMPLEMENTS_KEYWORD,
				JavaTokenType.IMPORT_KEYWORD,
				JavaTokenType.INSTANCEOF_KEYWORD,
				JavaTokenType.INT_KEYWORD,
				JavaTokenType.INTERFACE_KEYWORD,
				JavaTokenType.LONG_KEYWORD,
				JavaTokenType.NATIVE_KEYWORD,
				JavaTokenType.NEW_KEYWORD,
				JavaTokenType.PACKAGE_KEYWORD,
				JavaTokenType.PRIVATE_KEYWORD,
				JavaTokenType.PUBLIC_KEYWORD,
				JavaTokenType.SHORT_KEYWORD,
				JavaTokenType.SUPER_KEYWORD,
				JavaTokenType.SWITCH_KEYWORD,
				JavaTokenType.SYNCHRONIZED_KEYWORD,
				JavaTokenType.THIS_KEYWORD,
				JavaTokenType.THROW_KEYWORD,
				JavaTokenType.PROTECTED_KEYWORD,
				JavaTokenType.TRANSIENT_KEYWORD,
				JavaTokenType.RETURN_KEYWORD,
				JavaTokenType.VOID_KEYWORD,
				JavaTokenType.STATIC_KEYWORD,
				JavaTokenType.STRICTFP_KEYWORD,
				JavaTokenType.WHILE_KEYWORD,
				JavaTokenType.TRY_KEYWORD,
				JavaTokenType.VOLATILE_KEYWORD,
				JavaTokenType.THROWS_KEYWORD
		)
	}
}
