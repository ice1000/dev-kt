package org.ice1000.devkt.lang

import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lang.java.lexer.JavaLexer
import org.jetbrains.kotlin.com.intellij.lexer.EmptyLexer
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.PlainTextLanguage
import org.jetbrains.kotlin.com.intellij.pom.java.LanguageLevel
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KotlinLexer

/**
 * @author ice1000
 * @since v1.2
 * @see Language
 */
abstract class DevKtLanguage<TextAttributes> internal constructor(
		val language: Language
) : Annotator<TextAttributes>, SyntaxHighlighter<TextAttributes> {
	abstract fun satisfies(fileName: String): Boolean
	abstract val lineCommentStart: String
}

/**
 * @author ice1000
 * @since v1.2
 * @see DevKtLanguage
 */
abstract class DevKtLanguageBase<TextAttributes> internal constructor(
		private val annotator: Annotator<TextAttributes>,
		private val syntaxHighlighter: SyntaxHighlighter<TextAttributes>,
		val lexer: Lexer,
		language: Language
) : DevKtLanguage<TextAttributes>(language),
		Annotator<TextAttributes> by annotator,
		SyntaxHighlighter<TextAttributes> by syntaxHighlighter {
	override fun satisfies(fileName: String) = false
	override val lineCommentStart = "//"
}

/**
 * @author ice1000
 * @since v1.2
 */
abstract class ExtendedDevKtLanguage<TextAttributes>(
		language: Language,
		val parserDefinition: ParserDefinition
) : DevKtLanguage<TextAttributes>(language)

/**
 * @author ice1000
 * @since v1.2
 * @see JavaLanguage
 */
class Java<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : DevKtLanguageBase<TextAttributes>(
		annotator,
		syntaxHighlighter,
		JavaLexer(LanguageLevel.JDK_1_8), // TODO multiple language level support
		JavaLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".java")
}

/**
 * @author ice1000
 * @since v1.2
 * @see KotlinLanguage
 */
class Kotlin<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : DevKtLanguageBase<TextAttributes>(
		annotator,
		syntaxHighlighter,
		KotlinLexer(),
		KotlinLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".kt") or fileName.endsWith(".kts")
}

/**
 * @author ice1000
 * @since v1.2
 * @see PlainTextLanguage
 */
class PlainText<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : DevKtLanguageBase<TextAttributes>(
		annotator,
		syntaxHighlighter,
		EmptyLexer(),
		PlainTextLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".txt") || '.' !in fileName
}
