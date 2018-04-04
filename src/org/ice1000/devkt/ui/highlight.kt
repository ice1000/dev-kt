@file:Suppress("HasPlatformType")

package org.ice1000.devkt.ui

import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`
import org.ice1000.devkt.config.GlobalSettings
import sun.font.FontDesignMetrics
import java.awt.Color
import javax.swing.text.*

class ColorScheme(settings: GlobalSettings, context: AbstractDocument.AttributeContext = StyleContext.getDefaultStyleContext()) {
	val keywords = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorKeywords))
	val string = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorString))
	val templateEntries = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorTemplateEntries))
	val charLiteral = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorCharLiteral))
	val lineComments = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorLineComments))
	val blockComments = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorBlockComments))
	val docComments = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorDocComments))
	val operators = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorOperators))
	val parentheses = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorParentheses))
	val braces = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorBraces))
	val brackets = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorBrackets))
	val semicolon = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorSemicolon))
	val numbers = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorNumbers))
	val identifiers = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorIdentifiers))
	val annotations = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorAnnotations))
	val colon = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorColon))
	val comma = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorComma))
	val variable = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorVariable))
	val functionDeclaration = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colorFunction))
	val tabSize = createTabSizeAttributes(settings.tabSize)
}

/**
 * See https://stackoverflow.com/a/33557782/7083401
 * Modified a little
 */
fun createTabSizeAttributes(tabSize: Int): SimpleAttributeSet {
	val spaceSize = FontDesignMetrics.getMetrics(`{-# LANGUAGE SarasaGothicFont #-}`.monoFont).charWidth(' ')
	val tabWidth = spaceSize * tabSize
	val tabs = (1..300).map { TabStop((it * tabWidth).toFloat()) }
	val tabSet = TabSet(tabs.toTypedArray())
	val attributes = SimpleAttributeSet()
	StyleConstants.setTabSet(attributes, tabSet)
	return attributes
}
