package org.ice1000.devkt.kts

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*
import org.junit.Assert
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal const val NUM_4_LINE = "num: 4"

internal const val FIB_SCRIPT_OUTPUT_TAIL = """
fib(1)=1
fib(0)=1
fib(2)=2
fib(1)=1
fib(3)=3
fib(1)=1
fib(0)=1
fib(2)=2
fib(4)=5
"""

fun newConfiguration(): CompilerConfiguration {
	val configuration = CompilerConfiguration()
	configuration.put(CommonConfigurationKeys.MODULE_NAME, "www")
	if ("true" == System.getProperty("kotlin.ni")) {
		// Enable new inference for tests which do not declare their own language version settings
		configuration.languageVersionSettings = CompilerTestLanguageVersionSettings(emptyMap(),
				LanguageVersionSettingsImpl.DEFAULT.apiVersion,
				LanguageVersionSettingsImpl.DEFAULT.languageVersion,
				emptyMap())
	}

	configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, object : MessageCollector {
		override fun clear() = Unit
		override fun hasErrors() = false
		override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
			if (severity == CompilerMessageSeverity.ERROR) {
				val prefix = if (location == null) "" else "(${location.path}:${location.line}:${location.column}) "
				throw AssertionError(prefix + message)
			}
		}
	})
	return configuration
}

internal fun captureOut(body: () -> Unit): String {
	val outStream = ByteArrayOutputStream()
	val prevOut = System.out
	System.setOut(PrintStream(outStream))
	try {
		body()
	} finally {
		System.out.flush()
		System.setOut(prevOut)
	}
	return outStream.toString()
}

private fun String.linesSplitTrim() =
		split('\n', '\r').map(String::trim).filter(String::isNotBlank)

internal fun assertEqualsTrimmed(expected: String, actual: String) =
		Assert.assertEquals(expected.linesSplitTrim(), actual.linesSplitTrim())

data class CompilerTestLanguageVersionSettings(
		private val initialLanguageFeatures: Map<LanguageFeature, LanguageFeature.State>,
		override val apiVersion: ApiVersion,
		override val languageVersion: LanguageVersion,
		private val analysisFlags: Map<AnalysisFlag<*>, Any?> = emptyMap()
) : LanguageVersionSettings {
	private val languageFeatures = initialLanguageFeatures + specificFeaturesForTests()
	private val delegate = LanguageVersionSettingsImpl(languageVersion, apiVersion)

	override fun getFeatureSupport(feature: LanguageFeature): LanguageFeature.State =
			languageFeatures[feature] ?: delegate.getFeatureSupport(feature)

	@Suppress("UNCHECKED_CAST")
	override fun <T> getFlag(flag: AnalysisFlag<T>): T = analysisFlags[flag] as T? ?: flag.defaultValue
}

private fun specificFeaturesForTests(): Map<LanguageFeature, LanguageFeature.State> {
	return if (System.getProperty("kotlin.ni") == "true")
		mapOf(LanguageFeature.NewInference to LanguageFeature.State.ENABLED)
	else
		emptyMap()
}
