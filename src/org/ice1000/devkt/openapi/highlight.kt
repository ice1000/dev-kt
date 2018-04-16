package org.ice1000.devkt.openapi

import org.ice1000.devkt.config.GlobalSettings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

/**
 * @author ice1000
 */
interface AnnotationHolder<in TextAttributes> : LengthOwner {
	val text: String
	fun highlight(tokenStart: Int, tokenEnd: Int, attributeSet: TextAttributes)

	fun highlight(range: TextRange, attributeSet: TextAttributes) =
			highlight(range.startOffset, range.endOffset, attributeSet)

	fun highlight(astNode: ASTNode, attributeSet: TextAttributes) =
			highlight(astNode.textRange, attributeSet)

	fun highlight(element: PsiElement, attributeSet: TextAttributes) =
			highlight(element.textRange, attributeSet)
}

/**
 * @author ice1000
 * @since v1.2
 * @see com.intellij.openapi.fileTypes.SyntaxHighlighter
 */
interface SyntaxHighlighter<TextAttributes> {
	/**
	 * Highlight tokens. This is called after lexing.
	 *
	 * @param type IElementType token's type
	 * @param colorScheme ColorScheme<TextAttributes> the holder of colors
	 * @return TextAttributes? a member of [colorScheme]
	 * @see com.intellij.openapi.fileTypes.SyntaxHighlighter.getTokenHighlights
	 */
	fun attributesOf(type: IElementType, colorScheme: ColorScheme<TextAttributes>): TextAttributes?
}

/**
 * @author ice1000
 * @since v0.0.1
 * @see com.intellij.lang.annotation.Annotator
 * TODO move to daemon instead of running in ui thread
 */
interface Annotator<TextAttributes> {
	/**
	 * @param element the [PsiElement] to be highlighted
	 * @param document similar to [com.intellij.lang.annotation.AnnotationHolder]
	 * @param colorScheme current color scheme, initialized in [org.ice1000.devkt.config.GlobalSettings]
	 * @see com.intellij.lang.annotation.Annotator.annotate
	 */
	fun annotate(
			element: PsiElement,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>)
}

class ColorScheme<out TextAttributes>(
		settings: GlobalSettings,
		val tabSize: TextAttributes,
		wrapColor: (String) -> TextAttributes) {
	val keywords = wrapColor(settings.colorKeywords)
	val predefined = wrapColor(settings.colorPredefined)
	val string = wrapColor(settings.colorString)
	val stringEscape = wrapColor(settings.colorStringEscape)
	val interpolation = wrapColor(settings.colorInterpolation)
	val templateEntries = wrapColor(settings.colorTemplateEntries)
	val charLiteral = wrapColor(settings.colorCharLiteral)
	val lineComments = wrapColor(settings.colorLineComments)
	val blockComments = wrapColor(settings.colorBlockComments)
	val docComments = wrapColor(settings.colorDocComments)
	val operators = wrapColor(settings.colorOperators)
	val parentheses = wrapColor(settings.colorParentheses)
	val braces = wrapColor(settings.colorBraces)
	val brackets = wrapColor(settings.colorBrackets)
	val semicolon = wrapColor(settings.colorSemicolon)
	val numbers = wrapColor(settings.colorNumbers)
	val identifiers = wrapColor(settings.colorIdentifiers)
	val annotations = wrapColor(settings.colorAnnotations)
	val colon = wrapColor(settings.colorColon)
	val comma = wrapColor(settings.colorComma)
	val variable = wrapColor(settings.colorVariable)
	val function = wrapColor(settings.colorFunction)
	val typeParam = wrapColor(settings.colorTypeParam)
	val unknown = wrapColor(settings.colorUnknown)
	val error = wrapColor(settings.colorError)
	val userTypeRef = wrapColor(settings.colorUserTypeRef)
	val property = wrapColor(settings.colorProperty)
	val namespace = wrapColor(settings.colorNamespace)
	val metaData = wrapColor(settings.colorMetaData)
	val macro = wrapColor(settings.colorMacro)
}

