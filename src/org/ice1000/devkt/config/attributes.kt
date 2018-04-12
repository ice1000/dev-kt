@file:Suppress("HasPlatformType")

package org.ice1000.devkt.config

import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.font.FontRenderContext
import javax.swing.text.*

class ColorScheme<out TextAttributes>(
		settings: GlobalSettings,
		val tabSize: TextAttributes,
		wrapColor: (String) -> TextAttributes) {
	val keywords = wrapColor(settings.colorKeywords)
	val string = wrapColor(settings.colorString)
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
	val userTypeRef = wrapColor(settings.colorUserTypeRef)
	val property = wrapColor(settings.colorProperty)
}

fun swingColorScheme(
		settings: GlobalSettings,
		context: AbstractDocument.AttributeContext = StyleContext.getDefaultStyleContext()) =
		ColorScheme<AttributeSet>(settings, createTabSizeAttributes(settings.tabSize)) {
			context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(it))
		}

private fun createFrc() = FontRenderContext(GraphicsEnvironment
		.getLocalGraphicsEnvironment()
		.defaultScreenDevice
		.defaultConfiguration
		.defaultTransform
		, false, false)

/**
 * See https://stackoverflow.com/a/33557782/7083401
 * Modified a little
 */
fun createTabSizeAttributes(tabSize: Int): SimpleAttributeSet {
	val spaceSize = `{-# LANGUAGE SarasaGothicFont #-}`.monoFont.getStringBounds(" ", createFrc()).width
	val tabWidth = spaceSize * tabSize
	val tabs = (1..300).map { TabStop((it * tabWidth).toFloat()) }
	val tabSet = TabSet(tabs.toTypedArray())
	val attributes = SimpleAttributeSet()
	StyleConstants.setTabSet(attributes, tabSet)
	return attributes
}
