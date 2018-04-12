package org.ice1000.devkt.lang

import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lang.java.lexer.JavaLexer
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.pom.java.LanguageLevel
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KotlinLexer

/**
 * @author ice1000
 * @since v1.2
 * @see Language
 */
abstract class ProgrammingLanguage<TextAttributes>(
		private val annotator: Annotator<TextAttributes>,
		private val syntaxHighlighter: SyntaxHighlighter<TextAttributes>,
		val lexer: Lexer,
		val language: Language
) : Annotator<TextAttributes> by annotator, SyntaxHighlighter<TextAttributes> by syntaxHighlighter {
	abstract fun satisfies(fileName: String): Boolean
}

/**
 * @author ice1000
 * @since v1.2
 * @see JavaLanguage
 */
class Java<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : ProgrammingLanguage<TextAttributes>(
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
) : ProgrammingLanguage<TextAttributes>(
		annotator,
		syntaxHighlighter,
		KotlinLexer(),
		KotlinLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".kt") or fileName.endsWith(".kts")
}
