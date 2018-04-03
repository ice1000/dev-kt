@file:Suppress("HasPlatformType")

package org.ice1000.devkt

import java.awt.Color
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

class ColorScheme(settings: GlobalSettings) {
	private val cont = StyleContext.getDefaultStyleContext()
	val keywords = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.keywordsColor))
	val string = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.stringColor))
	val charLiteral = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.charLiteralColor))
	val lineComments = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.lineCommentsColor))
	val blockComments = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.blockCommentsColor))
	val docComments = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.docCommentsColor))
	val operators = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.operatorsColor))
	val parentheses = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.parenthesesColor))
	val braces = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.bracesColor))
	val brackets = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.bracketsColor))
	val semicolon = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.semicolonColor))
	val numbers = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.numbersColor))
	val others = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.othersColor))
	val colon = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.getColor(settings.colonColor))
}
