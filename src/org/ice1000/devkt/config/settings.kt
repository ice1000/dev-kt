package org.ice1000.devkt.config

import java.awt.Rectangle
import java.io.File
import java.util.*

/**
 * @author ice1000
 * @since v0.0.1
 */
class GlobalSettings {
	private val configFile = File("config.properties").absoluteFile
	private val properties = Properties()
	var lastOpenedFile: String by properties
	var tabSize: Int = 2
	var windowBounds = Rectangle(200, 100, 800, 600)
	var useTab: Boolean = true
	var recentFiles = hashSetOf<File>()

	var keywordsColor: String by properties
	var stringColor: String by properties
	var templateEntriesColor: String by properties
	var charLiteralColor: String by properties
	var lineCommentsColor: String by properties
	var blockCommentsColor: String by properties
	var docCommentsColor: String by properties
	var operatorsColor: String by properties
	var parenthesesColor: String by properties
	var bracesColor: String by properties
	var bracketsColor: String by properties
	var semicolonColor: String by properties
	var numbersColor: String by properties
	var identifiersColor: String by properties
	var annotationsColor: String by properties
	var colonColor: String by properties
	var commaColor: String by properties

	fun load() {
		if (!configFile.exists()) configFile.createNewFile()
		else properties.load(configFile.inputStream())
		if (!properties.containsKey(::lastOpenedFile.name)) lastOpenedFile = ""

		if (!properties.containsKey(::keywordsColor.name)) keywordsColor = "#CC7832"
		if (!properties.containsKey(::stringColor.name)) stringColor = "#6A8759"
		if (!properties.containsKey(::templateEntriesColor.name)) templateEntriesColor = "#CC7832"
		if (!properties.containsKey(::charLiteralColor.name)) charLiteralColor = "#6A8759"
		if (!properties.containsKey(::lineCommentsColor.name)) lineCommentsColor = "#808080"
		if (!properties.containsKey(::blockCommentsColor.name)) blockCommentsColor = "#808080"
		if (!properties.containsKey(::docCommentsColor.name)) docCommentsColor = "#629755"
		if (!properties.containsKey(::operatorsColor.name)) operatorsColor = "#A9B7C6"
		if (!properties.containsKey(::parenthesesColor.name)) parenthesesColor = "#A9B7C6"
		if (!properties.containsKey(::bracesColor.name)) bracesColor = "#A9B7C6"
		if (!properties.containsKey(::bracketsColor.name)) bracketsColor = "#A9B7C6"
		if (!properties.containsKey(::semicolonColor.name)) semicolonColor = "#CC7832"
		if (!properties.containsKey(::numbersColor.name)) numbersColor = "#6897BB"
		if (!properties.containsKey(::identifiersColor.name)) identifiersColor = "#A9B7C6"
		if (!properties.containsKey(::annotationsColor.name)) annotationsColor = "#BBB529"
		if (!properties.containsKey(::colonColor.name)) colonColor = "#A9B7C6"
		if (!properties.containsKey(::commaColor.name)) commaColor = "#CC7832"
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
		properties[::recentFiles.name]?.run {
			toString()
					.split(File.pathSeparatorChar)
					.mapNotNullTo(recentFiles) { File(it).takeIf { it.exists() } }
		}
	}

	fun save() {
		properties[::recentFiles.name] = recentFiles.joinToString(File.pathSeparator)
		properties[::useTab.name] = useTab
		properties[::tabSize.name] = tabSize
		properties[::windowBounds.name] = "${windowBounds.x},${windowBounds.y},${windowBounds.width},${windowBounds.height}"
		properties.store(configFile.outputStream(), null)
	}
}
