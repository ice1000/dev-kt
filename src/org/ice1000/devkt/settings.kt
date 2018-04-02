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

	fun load() {
		if (!configFile.exists()) configFile.createNewFile()
		else properties.load(configFile.inputStream())
	}

	fun save() {
		properties.store(configFile.outputStream(), null)
	}
}
