@file:Suppress("HasPlatformType")

package org.ice1000.devkt.ui

import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`
import org.ice1000.devkt.config.GlobalSettings
import sun.font.FontDesignMetrics
import java.awt.Color
import javax.swing.text.*

class ColorScheme(settings: GlobalSettings, context: AbstractDocument.AttributeContext = StyleContext.getDefaultStyleContext()) {
	val keywords = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.keywordsColor))
	val string = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.stringColor))
	val templateEntries = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.templateEntriesColor))
	val charLiteral = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.charLiteralColor))
	val lineComments = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.lineCommentsColor))
	val blockComments = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.blockCommentsColor))
	val docComments = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.docCommentsColor))
	val operators = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.operatorsColor))
	val parentheses = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.parenthesesColor))
	val braces = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.bracesColor))
	val brackets = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.bracketsColor))
	val semicolon = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.semicolonColor))
	val numbers = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.numbersColor))
	val identifiers = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.identifiersColor))
	val annotations = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.annotationsColor))
	val colon = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.colonColor))
	val comma = context.addAttribute(context.emptySet, StyleConstants.Foreground, Color.decode(settings.commaColor))
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
