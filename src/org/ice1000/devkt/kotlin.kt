package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.*
import java.io.File

data class ASTToken(
		val start: Int,
		val end: Int,
		val text: String,
		val type: IElementType
)

/**
 * @author ice1000
 * @since v0.0.1
 */
object Kotlin {
	val targetDir = File("./build-cache")
	val targetJar get() = targetDir.resolve(GlobalSettings.jarName)
	private val jvmEnvironment: KotlinCoreEnvironment
	private val jsEnvironment: KotlinCoreEnvironment
	private val psiFileFactory: PsiFileFactory
	private val lexer: KotlinLexer

	init {
		val compilerConfiguration = CompilerConfiguration()
		compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
		compilerConfiguration.put(JVMConfigurationKeys.OUTPUT_JAR, targetJar)
		compilerConfiguration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, targetDir)
		compilerConfiguration.addJvmClasspathRoot(File(selfLocation))
		// compilerConfiguration.put(JVMConfigurationKeys.IR, true)
		jvmEnvironment = KotlinCoreEnvironment.createForProduction(Disposable { },
				compilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
		val jsCompilerConfiguration = CompilerConfiguration()
		jsCompilerConfiguration.put(JSConfigurationKeys.OUTPUT_DIR, targetDir)
		jsEnvironment = KotlinCoreEnvironment.createForProduction(Disposable { },
				jsCompilerConfiguration, EnvironmentConfigFiles.JS_CONFIG_FILES)
		val project = jvmEnvironment.project
		psiFileFactory = PsiFileFactory.getInstance(project)
		val parserDef = KotlinParserDefinition.instance
		lexer = parserDef.createLexer(project) as KotlinLexer
	}

	fun parse(text: String) = psiFileFactory
			.createFileFromText(GlobalSettings.javaClassName, KotlinLanguage.INSTANCE, text) as KtFile

	fun compileJvm(ktFile: KtFile) {
		ensureTargetDirExists()
		compileFileTo(ktFile, jvmEnvironment, targetDir)
	}

	fun compileScript(scriptFile: File): Class<*>? {
		val configuration = jvmEnvironment.configuration.copy()
		configuration.addKotlinSourceRoot(scriptFile.absolutePath)
		configuration.put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
		configuration.add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, KotlinScriptDefinition(Any::class))
		val scriptEnvironment = KotlinCoreEnvironment.createForProduction(Disposable { },
				configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
		val scriptDefinitionProvider = ScriptDefinitionProvider.getInstance(scriptEnvironment.project)
		val error = scriptFile.isDirectory || !scriptDefinitionProvider.isScript(scriptFile.name)
		if (error) return null
		val state = KotlinToJVMBytecodeCompiler.compileScript(scriptEnvironment, javaClass.classLoader)
		println(state?.javaClass)
		return state
	}

	fun compileJar(ktFile: KtFile) {
		ensureTargetDirExists()
		CompileEnvironmentUtil.writeToJar(
				targetJar,
				false,
				FqName.fromSegments(listOf("devkt", "${GlobalSettings.javaClassName}Kt")),
				compileFile(ktFile, jvmEnvironment))
	}

	fun compileJs(ktFile: KtFile) {
		ensureTargetDirExists()
		TODO()
	}

	private fun ensureTargetDirExists() {
		if (!targetDir.isDirectory) targetDir.mkdirs()
		targetDir.listFiles().forEach { it.deleteRecursively() }
	}

	// TODO incremental
	fun lex(text: String) = lexer.run {
		start(text)
		generateSequence {
			tokenType
					?.let { ASTToken(tokenStart, tokenEnd, tokenText, it) }
					?.also { advance() }
		}
	}
}

val stringTokens = TokenSet.create(
		KtTokens.OPEN_QUOTE,
		KtTokens.CLOSING_QUOTE,
		KtTokens.REGULAR_STRING_PART
)

val stringTemplateTokens = TokenSet.create(
		KtTokens.SHORT_TEMPLATE_ENTRY_START,
		KtTokens.LONG_TEMPLATE_ENTRY_START,
		KtTokens.LONG_TEMPLATE_ENTRY_END
)

