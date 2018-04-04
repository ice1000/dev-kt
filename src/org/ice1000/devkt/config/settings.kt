package org.ice1000.devkt.config

import java.awt.Rectangle
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * @author ice1000
 * @since v0.0.1
 */
object GlobalSettings {
	private val configFile = File("config.properties").absoluteFile
	private val properties = Properties()
	var lastOpenedFile: String by properties
	var tabSize: Int = 2
	var windowBounds = Rectangle(200, 100, 800, 600)
	var windowIcon = "" to ImageIO.read(javaClass.getResourceAsStream("/icon/kotlin24@2x.png"))
	var useTab: Boolean = true
	var highlightTokenBased: Boolean = true
	var highlightSemanticBased: Boolean = true
	var recentFiles = hashSetOf<File>()

	var appName: String by properties
	var monoFontName: String by properties
	var gothicFontName: String by properties
	var colorKeywords: String by properties
	var colorString: String by properties
	var colorTemplateEntries: String by properties
	var colorCharLiteral: String by properties
	var colorLineComments: String by properties
	var colorBlockComments: String by properties
	var colorDocComments: String by properties
	var colorOperators: String by properties
	var colorParentheses: String by properties
	var colorBraces: String by properties
	var colorBrackets: String by properties
	var colorSemicolon: String by properties
	var colorNumbers: String by properties
	var colorIdentifiers: String by properties
	var colorAnnotations: String by properties
	var colorColon: String by properties
	var colorComma: String by properties

	fun load() {
		if (!configFile.exists()) configFile.createNewFile()
		else properties.load(configFile.inputStream())
		if (!properties.containsKey(::lastOpenedFile.name)) lastOpenedFile = ""
		if (!properties.containsKey(::appName.name)) appName = "Dev Kt"
		if (!properties.containsKey(::monoFontName.name)) monoFontName = ""
		if (!properties.containsKey(::gothicFontName.name)) gothicFontName = ""
		if (!properties.containsKey(::colorKeywords.name)) colorKeywords = "#CC7832"
		if (!properties.containsKey(::colorString.name)) colorString = "#6A8759"
		if (!properties.containsKey(::colorTemplateEntries.name)) colorTemplateEntries = "#CC7832"
		if (!properties.containsKey(::colorCharLiteral.name)) colorCharLiteral = "#6A8759"
		if (!properties.containsKey(::colorLineComments.name)) colorLineComments = "#808080"
		if (!properties.containsKey(::colorBlockComments.name)) colorBlockComments = "#808080"
		if (!properties.containsKey(::colorDocComments.name)) colorDocComments = "#629755"
		if (!properties.containsKey(::colorOperators.name)) colorOperators = "#A9B7C6"
		if (!properties.containsKey(::colorParentheses.name)) colorParentheses = "#A9B7C6"
		if (!properties.containsKey(::colorBraces.name)) colorBraces = "#A9B7C6"
		if (!properties.containsKey(::colorBrackets.name)) colorBrackets = "#A9B7C6"
		if (!properties.containsKey(::colorSemicolon.name)) colorSemicolon = "#CC7832"
		if (!properties.containsKey(::colorNumbers.name)) colorNumbers = "#6897BB"
		if (!properties.containsKey(::colorIdentifiers.name)) colorIdentifiers = "#A9B7C6"
		if (!properties.containsKey(::colorAnnotations.name)) colorAnnotations = "#BBB529"
		if (!properties.containsKey(::colorColon.name)) colorColon = "#A9B7C6"
		if (!properties.containsKey(::colorComma.name)) colorComma = "#CC7832"
		properties[::windowIcon.name]
				?.toString()
				?.also {
					try {
						windowIcon = it to ImageIO.read(File(it))
					} catch (ignored: Exception) {
					}
				}
		properties[::windowBounds.name]
				?.toString()
				?.split(',', limit = 4)
				?.also { (x, y, width, height) ->
					x.toIntOrNull()?.let { windowBounds.x = it }
					y.toIntOrNull()?.let { windowBounds.y = it }
					width.toIntOrNull()?.let { windowBounds.width = it }
					height.toIntOrNull()?.let { windowBounds.height = it }
				}
		properties[::tabSize.name]?.toString()?.toIntOrNull()?.let { tabSize = it }
		properties[::useTab.name]?.let { useTab = it.toString() == "true" }
		properties[::highlightTokenBased.name]?.let { highlightTokenBased = it.toString() == "true" }
		properties[::highlightSemanticBased.name]?.let { highlightSemanticBased = it.toString() == "true" }
		properties[::recentFiles.name]?.run {
			toString()
					.split(File.pathSeparatorChar)
					.mapNotNullTo(recentFiles) { File(it).takeIf { it.exists() } }
		}
	}

	fun save() {
		properties[::recentFiles.name] = recentFiles.joinToString(File.pathSeparator)
		properties[::useTab.name] = useTab.toString()
		properties[::highlightTokenBased.name] = highlightTokenBased.toString()
		properties[::highlightSemanticBased.name] = highlightSemanticBased.toString()
		properties[::tabSize.name] = tabSize.toString()
		properties[::windowBounds.name] = "${windowBounds.x},${windowBounds.y},${windowBounds.width},${windowBounds.height}"
		properties[::windowIcon.name] = windowIcon.first
		properties.store(configFile.outputStream(), null)
	}
}
