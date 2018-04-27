package org.ice1000.devkt

import org.ice1000.devkt.config.GlobalSettings
import org.ice1000.devkt.lang.DevKtLanguage
import org.ice1000.devkt.openapi.ExtendedDevKtLanguage
import org.ice1000.devkt.openapi.nodeType
import org.ice1000.devkt.openapi.util.selfLocation
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.CompileEnvironmentUtil
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.com.intellij.lang.Language
import org.jetbrains.kotlin.com.intellij.lang.LanguageExtension
import org.jetbrains.kotlin.com.intellij.lang.LanguageParserDefinitions
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lexer.Lexer
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.io.File

data class ASTToken(
		val start: Int,
		val end: Int,
		val text: CharSequence,
		val type: IElementType
) {
	constructor(node: PsiElement) : this(node.startOffset, node.endOffset, node.text, node.nodeType)

	val textLength get() = text.length
}

@Suppress("unused")
/**
 * @author ice1000
 * @since v0.0.1
 */
object Analyzer : Disposable {
	val targetDir = File("./.build-cache")
	val targetJar get() = targetDir.resolve(GlobalSettings.jarName)
	private val scriptEngine = DevKtScriptEngineFactory.scriptEngine
	// private val originalStdout = System.out
	// private val originalStderr = System.err
	private val jvmEnvironment: KotlinCoreEnvironment
	private val jsEnvironment: KotlinCoreEnvironment
	private val psiFileFactory: PsiFileFactory
	val project: Project

	override fun dispose() = Unit

	init {
		val compilerConfiguration = CompilerConfiguration()
		compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
		compilerConfiguration.put(JVMConfigurationKeys.OUTPUT_JAR, targetJar)
		compilerConfiguration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, targetDir)
		compilerConfiguration.addJvmClasspathRoot(File(selfLocation))
		// compilerConfiguration.put(JVMConfigurationKeys.IR, true)
		jvmEnvironment = KotlinCoreEnvironment.createForProduction(this,
				compilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
		val jsCompilerConfiguration = CompilerConfiguration()
		jsCompilerConfiguration.put(JSConfigurationKeys.OUTPUT_DIR, targetDir)
		jsEnvironment = KotlinCoreEnvironment.createForProduction(this,
				jsCompilerConfiguration, EnvironmentConfigFiles.JS_CONFIG_FILES)
		project = jvmEnvironment.project
		psiFileFactory = PsiFileFactory.getInstance(project)
	}

	private fun <Extension> registerExtensionPoint(
			extensionPoint: LanguageExtension<Extension>,
			language: Language,
			instance: Extension) {
		extensionPoint.addExplicitExtension(language, instance)
		Disposer.register(project, Disposable {
			extensionPoint.removeExplicitExtension(language, instance)
		})
	}

//	private fun <Extension> registerExtensionPoint(
//			extensionPoint: ExtensionPointName<Extension>,
//			clazz: Class<Extension>) = registerExtensionPoint(extensionPoint, clazz.newInstance())

	fun registerLanguage(
			language: Language, parserDefinition: ParserDefinition) {
		LanguageParserDefinitions.INSTANCE.addExplicitExtension(language, parserDefinition)
		Disposer.register(project, Disposable {
			LanguageParserDefinitions.INSTANCE.removeExplicitExtension(language, parserDefinition)
		})
	}

	fun registerLanguage(
			extendedProgrammingLanguage: ExtendedDevKtLanguage<*>) = registerLanguage(
			extendedProgrammingLanguage.language,
			extendedProgrammingLanguage.parserDefinition)

	fun parseKotlin(text: String) = parse(text, KotlinLanguage.INSTANCE) as KtFile
	fun parseJava(text: String) = parse(text, JavaLanguage.INSTANCE) as PsiJavaFile
	fun parse(text: String, language: DevKtLanguage<*>, name: String? = null) = parse(text, language.language, name)
	fun parse(text: String, language: Language, name: String? = null) = psiFileFactory
			.createFileFromText(name ?: GlobalSettings.javaClassName, language, text, false, false, true)

	fun compileJvm(ktFile: KtFile) {
		ensureTargetDirExists()
		compileFileTo(ktFile, jvmEnvironment, targetDir)
	}

	fun runScript(text: String) {
		scriptEngine.eval(text)
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
	fun lex(text: String, lexer: Lexer) = lexer.run {
		start(text)
		generateSequence {
			tokenType
					?.let { ASTToken(tokenStart, tokenEnd, tokenSequence, it) }
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

