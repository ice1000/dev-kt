package org.ice1000.devkt.lang

import org.ice1000.devkt.openapi.Annotator
import org.ice1000.devkt.openapi.SyntaxHighlighter
import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lang.java.lexer.JavaLexer
import org.jetbrains.kotlin.com.intellij.lexer.EmptyLexer
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.PlainTextLanguage
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
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
	abstract fun createLexer(project: Project): Lexer
}

/**
 * @author ice1000
 * @since v1.2
 * @see DevKtLanguage
 */
abstract class DevKtLanguageBase<TextAttributes> internal constructor(
		private val annotator: Annotator<TextAttributes>,
		private val syntaxHighlighter: SyntaxHighlighter<TextAttributes>,
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
 * @see JavaLanguage
 */
class Java<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : DevKtLanguageBase<TextAttributes>(
		annotator,
		syntaxHighlighter,
		JavaLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".java")
	private val java8Lexer = JavaLexer(LanguageLevel.JDK_1_8)
	// TODO multiple language level support
	override fun createLexer(project: Project) = java8Lexer
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
		KotlinLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".kt") or fileName.endsWith(".kts")
	private val lexer = KotlinLexer()
	override fun createLexer(project: Project) = lexer
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
		PlainTextLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".txt") || '.' !in fileName
	private val lexer = EmptyLexer()
	override fun createLexer(project: Project) = lexer
}
