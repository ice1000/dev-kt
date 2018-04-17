package org.ice1000.devkt.kts

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory
import javax.script.ScriptEngineManager
import kotlin.test.Test
import kotlin.test.assertEquals

class EngineTest {
	@Test
	fun testJsr223() {
		val factory = ScriptEngineManager().getEngineByExtension("kts")!!
		//language=kotlin
		assertEquals(2, factory.eval("{1+1}()"))
		//language=kotlin
		assertEquals(3, factory.eval("1.let(2::plus)"))
	}

	@Test
	fun testNew() {
		val factory = KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory()
				.scriptEngine
		//language=kotlin
		assertEquals(2, factory.eval("{1+1}()"))
		//language=kotlin
		assertEquals(3, factory.eval("1.let(2::plus)"))
	}

	@Test
	fun testNewDaemon() {
		val factory = KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory()
				.scriptEngine
		//language=kotlin
		assertEquals(2, factory.eval("{1+1}()"))
		//language=kotlin
		assertEquals(3, factory.eval("1.let(2::plus)"))
	}
}
