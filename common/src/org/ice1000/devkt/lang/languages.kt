package org.ice1000.devkt.lang

import org.ice1000.devkt.openapi.Annotator
import org.ice1000.devkt.openapi.SyntaxHighlighter
import org.ice1000.devkt.openapi.ui.IDevKtDocumentHandler
import org.ice1000.devkt.openapi.util.CompletionElement
import org.ice1000.devkt.ui.DevKtIcons
import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lang.java.lexer.JavaLexer
import org.jetbrains.kotlin.com.intellij.lexer.EmptyLexer
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.PlainTextLanguage
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.pom.java.LanguageLevel
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KotlinLexer
import javax.swing.Icon

/**
 * @author ice1000
 * @since v1.2
 * @see Language
 */
interface DevKtLanguage<TextAttributes> : Annotator<TextAttributes>, SyntaxHighlighter<TextAttributes> {
	val language: Language

	/**
	 * Check if a file is of this language
	 * @param fileName String the file name
	 * @return Boolean is of this language or not
	 */
	@JvmDefault
	fun satisfies(fileName: String) = false

	/**
	 * Line comment start, used when pressing <kbd>Ctrl</kbd> + <kbd>/</kbd>
	 */
	@JvmDefault
	val lineCommentStart: String?
		get() = null

	/**
	 * Block comment surrounding, used when pressing
	 * <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>/</kbd>
	 */
	@JvmDefault
	val blockComment: Pair<String, String>?
		get() = null

	@JvmDefault
	val icon: Icon
		get() = DevKtIcons.ANY

	fun createLexer(project: Project): Lexer

	/**
	 * Called when typing, before the typed character is inserted.
	 *
	 * @param offset Int
	 * @param text String?
	 * @param element PsiElement?
	 * @param document DevKtDocument<TextAttributes>
	 * @see org.ice1000.devkt.ui.DevKtDocumentHandler.insertDirectly
	 * @see org.ice1000.devkt.ui.DevKtDocumentHandler.insert
	 */
	@JvmDefault
	fun handleTyping(
			offset: Int,
			text: String?,
			element: PsiElement?,
			document: IDevKtDocumentHandler<TextAttributes>) {
		document.insert(offset, text)
	}

	@JvmDefault
	val initialCompletionElementList: Set<CompletionElement>
		get() = emptySet()
}

/**
 * @author ice1000
 * @since v1.2
 * @see DevKtLanguage
 */
sealed class BuiltinDevKtLanguage<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>,
		override val language: Language
) : DevKtLanguage<TextAttributes>,
		Annotator<TextAttributes> by annotator,
		SyntaxHighlighter<TextAttributes> by syntaxHighlighter {
	override fun satisfies(fileName: String) = false
	override val lineCommentStart = "//"
	override val blockComment: Pair<String, String>? = "/*" to "*/"
	override fun handleTyping(
			offset: Int,
			text: String?,
			element: PsiElement?,
			document: IDevKtDocumentHandler<TextAttributes>) = element.takeIf { text == "\n" }?.run {
		val whitespace = containingFile.findElementAt(document.startOffsetOf(document.lineOf(offset)))
				as? PsiWhiteSpace ?: return@run null
		super.handleTyping(offset, whitespace.text, element, document)
	} ?: super.handleTyping(offset, text, element, document)
}

/**
 * @author ice1000
 * @since v1.2
 * @see JavaLanguage
 */
class Java<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : BuiltinDevKtLanguage<TextAttributes>(
		annotator,
		syntaxHighlighter,
		JavaLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".java")
	private val java8Lexer = JavaLexer(LanguageLevel.JDK_1_8)
	// TODO multiple language level support
	override fun createLexer(project: Project) = java8Lexer

	override val icon: Icon get() = DevKtIcons.JAVA
}

/**
 * @author ice1000
 * @since v1.2
 * @see KotlinLanguage
 */
class Kotlin<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : BuiltinDevKtLanguage<TextAttributes>(
		annotator,
		syntaxHighlighter,
		KotlinLanguage.INSTANCE) {
	override fun satisfies(fileName: String) = fileName.endsWith(".kt") or fileName.endsWith(".kts")
	private val lexer = KotlinLexer()
	override fun createLexer(project: Project) = lexer
	override val icon: Icon get() = DevKtIcons.KOTLIN
}

/**
 * @author ice1000
 * @since v1.2
 * @see PlainTextLanguage
 */
class PlainText<TextAttributes>(
		annotator: Annotator<TextAttributes>,
		syntaxHighlighter: SyntaxHighlighter<TextAttributes>
) : DevKtLanguage<TextAttributes>,
		Annotator<TextAttributes> by annotator,
		SyntaxHighlighter<TextAttributes> by syntaxHighlighter {
	override val language: PlainTextLanguage = PlainTextLanguage.INSTANCE
	override fun satisfies(fileName: String) =
			fileName.endsWith(".txt") && !fileName.endsWith(".lua.txt") || '.' !in fileName

	private val lexer = EmptyLexer()
	override fun createLexer(project: Project) = lexer
}
