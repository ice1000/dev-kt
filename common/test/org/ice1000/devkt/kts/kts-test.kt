package org.ice1000.devkt.kts

import org.ice1000.devkt.Analyzer
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
import org.junit.Assert.assertEquals
import org.junit.Ignore
import java.io.File
import kotlin.script.templates.ScriptTemplateDefinition

@ScriptTemplateDefinition
open class AbstractScriptTemplate(val args: Array<String>)

@ScriptTemplateDefinition
open class ScriptTemplate(args: Array<String>) : AbstractScriptTemplate(args)

@Ignore
class ScriptTest {
	fun standardScriptWithParams() {
		val aClass = compileScript("die_home_guy_so_disgusting.kts", StandardScriptDefinition)
		Assert.assertNotNull(aClass)
		val out = captureOut {
			val anObj = tryConstructClassFromStringArgs(aClass!!, listOf("4", "commentCurrent"))
			Assert.assertNotNull(anObj)
		}
		// 	assertEqualsTrimmed("$NUM_4_LINE (commentCurrent)$FIB_SCRIPT_OUTPUT_TAIL", out)
	}

	fun standardScriptWithoutParams() {
		val aClass = compileScript("die_home_guy_so_disgusting.kts",
				KotlinScriptDefinition(ScriptTemplate::class))!!
		val out = captureOut {
			val anObj = tryConstructClassFromStringArgs(aClass, emptyList())
			Assert.assertNotNull(anObj)
		}
		assertEqualsTrimmed("我永远喜欢灵乌路空", out)
	}

	fun useCompilerInternals() {
		val scriptClass = compileScript("use_compiler_internals.kts",
				KotlinScriptDefinition(ScriptTemplate::class), false)!!
		assertEquals("OK", captureOut {
			tryConstructClassFromStringArgs(scriptClass, emptyList())
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
			configuration.addKotlinSourceRoot("testRes/script/$scriptPath")
			configuration.add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, scriptDefinition)
			configuration.put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
			if (saveClassesDir != null) configuration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, saveClassesDir)
			val environment = KotlinCoreEnvironment.createForTests(rootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
			return try {
				KotlinToJVMBytecodeCompiler.compileScript(environment, javaClass.classLoader.takeUnless { runIsolated })
			} catch (e: CompilationException) {
				messageCollector.report(CompilerMessageSeverity.EXCEPTION, OutputMessageUtil.renderException(e),
						MessageUtil.psiElementToMessageLocation(e.element))
				null
			} catch (t: Throwable) {
				MessageCollectorUtil.reportException(messageCollector, t)
				throw t
			}
		} finally {
			Disposer.dispose(rootDisposable)
		}
	}
}

fun main(args: Array<String>) {
	val file = File("res/template/script.kts")
	GlobalSettings.load()
	Analyzer.runScript(file.readText())
}


