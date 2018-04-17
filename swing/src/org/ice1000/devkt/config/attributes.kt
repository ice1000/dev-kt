@file:Suppress("HasPlatformType")

package org.ice1000.devkt.config

import org.ice1000.devkt.DevKtFontManager
import org.ice1000.devkt.openapi.ColorScheme
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.font.FontRenderContext
import javax.swing.text.*

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
	val spaceSize = DevKtFontManager.monoFont.getStringBounds(" ", createFrc()).width
	val tabWidth = spaceSize * tabSize
	val tabs = (1..300).map { TabStop((it * tabWidth).toFloat()) }
	val tabSet = TabSet(tabs.toTypedArray())
	val attributes = SimpleAttributeSet()
	StyleConstants.setTabSet(attributes, tabSet)
	return attributes
}
