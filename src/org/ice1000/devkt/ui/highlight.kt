@file:Suppress("HasPlatformType")

package org.ice1000.devkt.ui

import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`
import org.ice1000.devkt.config.GlobalSettings
import sun.font.FontDesignMetrics
import java.awt.Color
import javax.swing.text.*

class ColorScheme(settings: GlobalSettings) {
	private val cont = StyleContext.getDefaultStyleContext()
	val keywords = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.keywordsColor))
	val string = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.stringColor))
	val charLiteral = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.charLiteralColor))
	val lineComments = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.lineCommentsColor))
	val blockComments = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.blockCommentsColor))
	val docComments = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.docCommentsColor))
	val operators = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.operatorsColor))
	val parentheses = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.parenthesesColor))
	val braces = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.bracesColor))
	val brackets = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.bracketsColor))
	val semicolon = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.semicolonColor))
	val numbers = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.numbersColor))
	val others = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.othersColor))
	val colon = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.colonColor))
	val comma = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.decode(settings.commaColor))
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
