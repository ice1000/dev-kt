package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestProperties {
	@Test
	fun init() {
		GlobalSettings::class.declaredMemberProperties.forEach {
			if ("configFile" == it.name || "properties" == it.name) return@forEach
			it.isAccessible = true
			assertNotNull(
					it.getDelegate(GlobalSettings) ?: it.get(GlobalSettings),
					"${it.name} should not be null")
		}
	}
}
