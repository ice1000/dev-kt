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
	var useTab: Boolean
		get() = useTabImpl == "1"
		set(value) {
			useTabImpl = if (value) "1" else ""
		}

	fun load() {
		if (!configFile.exists()) configFile.createNewFile()
		else properties.load(configFile.inputStream())
		if (!properties.containsKey(::useTabImpl.name)) useTabImpl = "1"
	}

	fun save() {
		properties.store(configFile.outputStream(), null)
	}
}
