package org.ice1000.devkt

import java.io.File
import java.util.*

/**
 * @author ice1000
 * @since v0.0.1
 */
object GlobalSettings {
	private val configFile = File("config.properties").absoluteFile
	private val properties = Properties()
	private var useTabImpl: String by properties
	private var tabSizeImpl: String by properties
	var lastOpenedFile: String by properties
	var tabSize: Int
		get() = tabSizeImpl.toInt()
		set(value) {
			tabSizeImpl = value.toString()
		}
	var useTab: Boolean
		get() = useTabImpl == "1"
		set(value) {
			useTabImpl = if (value) "1" else ""
		}

	var keywordsColor: String by properties
	var stringColor: String by properties
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
	var othersColor: String by properties
	var colonColor: String by properties
	var commaColor: String by properties

	fun load() {
		if (!configFile.exists()) configFile.createNewFile()
		else properties.load(configFile.inputStream())
		if (!properties.containsKey(::useTabImpl.name)) useTabImpl = "1"
		if (!properties.containsKey(::tabSizeImpl.name)) tabSizeImpl = "3"
		if (!properties.containsKey(::lastOpenedFile.name)) lastOpenedFile = ""

		if (!properties.containsKey(::keywordsColor.name)) keywordsColor = "#CC7832"
		if (!properties.containsKey(::stringColor.name)) stringColor = "#6A8759"
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
		if (!properties.containsKey(::othersColor.name)) othersColor = "#A9B7C6"
		if (!properties.containsKey(::colonColor.name)) colonColor = "#A9B7C6"
		if (!properties.containsKey(::commaColor.name)) commaColor = "#CC7832"
	}

	fun save() {
		properties.store(configFile.outputStream(), null)
	}
}
