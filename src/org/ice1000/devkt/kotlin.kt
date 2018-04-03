package org.ice1000.devkt

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPoint
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions.getArea
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.TreeCopyHandler
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.preprocessor.mkdirsOrFail
import org.jetbrains.kotlin.psi.KtFile
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
	private val environment: KotlinCoreEnvironment
	private val psiFileFactory: PsiFileFactory
	private val lexer: KotlinLexer

	init {
		val compilerConfiguration = CompilerConfiguration()
		compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
		environment = KotlinCoreEnvironment.createForProduction(Disposable {},
				compilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
		val project = environment.project
		val extensionPoint = "org.jetbrains.kotlin.com.intellij.treeCopyHandler"
		val extensionClassName = TreeCopyHandler::class.java.name.orEmpty()
		for (area in arrayOf(getArea(project), getArea(null))) {
			if (!area.hasExtensionPoint(extensionPoint)) {
				area.registerExtensionPoint(extensionPoint, extensionClassName, ExtensionPoint.Kind.INTERFACE)
			}
		}
		psiFileFactory = PsiFileFactory.getInstance(project)
		val parserDef = KotlinParserDefinition.instance
		lexer = parserDef.createLexer(project) as KotlinLexer
	}

	fun parse(text: String) = psiFileFactory
			.createFileFromText(KotlinLanguage.INSTANCE, text) as? KtFile

	fun compile(ktFile: KtFile) {
		val file = File("./aaa")
		if (!file.exists()) file.mkdirs()
		compileFileTo(ktFile, environment, file)
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
