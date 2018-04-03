@file:Suppress("HasPlatformType")

package org.ice1000.devkt

import java.awt.Color
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

private val cont = StyleContext.getDefaultStyleContext()
val KEYWORDS = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.ORANGE)
val STRING = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.GREEN)
val OTHERS = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.LIGHT_GRAY)
val COMMENTS = cont.addAttribute(cont.emptySet, StyleConstants.Foreground, Color.GRAY)

