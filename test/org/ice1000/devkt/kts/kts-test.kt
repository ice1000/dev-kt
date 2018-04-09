package org.ice1000.devkt.kts

import org.ice1000.devkt.Kotlin
import org.ice1000.devkt.config.GlobalSettings
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.addKotlinSourceRoot
import org.jetbrains.kotlin.script.*
import org.junit.Assert
import java.io.File

class ScriptTest : KtUsefulTestCase() {
	fun testStandardScriptWithParams() {
		val aClass = compileScript("fib_std.kts", StandardScriptDefinition)
		Assert.assertNotNull(aClass)
		val out = captureOut {
			val anObj = tryConstructClassFromStringArgs(aClass!!, listOf("4", "comment"))
			Assert.assertNotNull(anObj)
		}
		assertEqualsTrimmed("$NUM_4_LINE (comment)$FIB_SCRIPT_OUTPUT_TAIL", out)
	}

	fun testStandardScriptWithoutParams() {
		val aClass = compileScript("fib_std.kts", StandardScriptDefinition)
		Assert.assertNotNull(aClass)
		val out = captureOut {
			val anObj = tryConstructClassFromStringArgs(aClass!!, emptyList())
			Assert.assertNotNull(anObj)
		}
		assertEqualsTrimmed("$NUM_4_LINE (none)$FIB_SCRIPT_OUTPUT_TAIL", out)
	}

	fun testUseCompilerInternals() {
		val scriptClass = compileScript("use_compiler_internals.kts", StandardScriptDefinition)!!
		assertEquals("OK", captureOut {
			tryConstructClassFromStringArgs(scriptClass, emptyList())!!
		})
	}

	private fun compileScript(
			scriptPath: String,
			scriptDefinition: KotlinScriptDefinition,
			runIsolated: Boolean = true,
			suppressOutput: Boolean = false,
			saveClassesDir: File? = null
	): Class<*>? {
		val messageCollector =
				if (suppressOutput) MessageCollector.NONE
				else PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)

		val rootDisposable = Disposer.newDisposable()
		try {
			val configuration = newConfiguration()
			configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
			configuration.addKotlinSourceRoot("compiler/testData/script/$scriptPath")
			configuration.add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, scriptDefinition)
			configuration.put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
			if (saveClassesDir != null) {
				configuration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, saveClassesDir)
			}

			val environment = KotlinCoreEnvironment.createForTests(rootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)

			try {
				return KotlinToJVMBytecodeCompiler.compileScript(environment, this::class.java.classLoader.takeUnless { runIsolated })
			}
			catch (e: CompilationException) {
				messageCollector.report(CompilerMessageSeverity.EXCEPTION, OutputMessageUtil.renderException(e),
						MessageUtil.psiElementToMessageLocation(e.element))
				return null
			}
			catch (t: Throwable) {
				MessageCollectorUtil.reportException(messageCollector, t)
				throw t
			}
		}
		finally {
			Disposer.dispose(rootDisposable)
		}
	}
}

fun main(args: Array<String>) {
	val file = File("res/template/script.kts")
	GlobalSettings.load()
	Kotlin.compileScript(file)
}


