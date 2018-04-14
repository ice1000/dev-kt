package org.ice1000.devkt.openapi

import org.ice1000.devkt.lang.DevKtLanguage
import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.project.Project


/**
 * @author ice1000
 * @since v1.2
 */
abstract class ExtendedDevKtLanguage<TextAttributes>(
		language: Language,
		val parserDefinition: ParserDefinition
) : DevKtLanguage<TextAttributes>(language) {
	override fun createLexer(project: Project): Lexer = parserDefinition.createLexer(project)
}
