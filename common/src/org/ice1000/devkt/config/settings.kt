package org.ice1000.devkt.config

import org.ice1000.devkt.defaultFontName
import org.ice1000.devkt.openapi.util.handleException
import org.ice1000.devkt.openapi.util.ignoreException
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.reflect.KMutableProperty

/**
 * @author ice1000
 * @since v0.0.1
 */
object GlobalSettings {
	private val configFile = File("config.properties").absoluteFile
	private val properties = Properties()
	var lastOpenedFile: String by properties
	var tabSize: Int = 2
	var psiViewerMaxCodeLength: Int = 30
	var backgroundAlpha: Int = 180
	var fontSize: Float = 16F
	var windowBounds = Rectangle(200, 100, 800, 600)
	var windowIcon = "" to ImageIO.read(javaClass.getResourceAsStream("/icon/kotlin@288x288.png"))
	var backgroundImage: Pair<String, BufferedImage?> = "" to null
	var useTab: Boolean = true
	var highlightTokenBased: Boolean = true
	var highlightSemanticBased: Boolean = true
	var recentFiles: MutableSet<File> = hashSetOf()

	var javaClassName: String by properties
	var jarName: String by properties
	var appName: String by properties
	var monoFontName: String by properties
	var gothicFontName: String by properties
	var colorDefault: String by properties
	var colorKeywords: String by properties
	var colorPredefined: String by properties
	var colorString: String by properties
	var colorStringEscape: String by properties
	var colorInterpolation: String by properties
	var colorTemplateEntries: String by properties
	var colorCharLiteral: String by properties
	var colorLineComments: String by properties
	var colorBlockComments: String by properties
	var colorDocComments: String by properties
	var colorOperators: String by properties
	var colorParentheses: String by properties
	var colorBraces: String by properties
	var colorBrackets: String by properties
	var colorSemicolon: String by properties
	var colorNumbers: String by properties
	var colorIdentifiers: String by properties
	var colorAnnotations: String by properties
	var colorColon: String by properties
	var colorComma: String by properties
	var colorVariable: String by properties
	var colorFunction: String by properties
	var colorUnknown: String by properties
	var colorError: String by properties
	var colorTypeParam: String by properties
	var colorUserTypeRef: String by properties
	var colorProperty: String by properties
	var colorNamespace: String by properties
	var colorMetaData: String by properties
	var colorMacro: String by properties
	var colorBackground: String by properties
	var colorInputError: String by properties

	var shortcutUndo = ShortCut(true, false, false, Key.Z)
	var shortcutSave = ShortCut(true, false, false, Key.S)
	var shortcutRedo = ShortCut(true, false, true, Key.Z)
	var shortcutSync = ShortCut(true, true, false, Key.Y)
	var shortcutGoto = ShortCut(true, false, false, Key.G)
	var shortcutOpen = ShortCut(true, false, false, Key.O)
	// Build | Run As | Class, force to use ctrl + R even though it is in Mac.
	var shortcutBuildRunAsClass = ShortCut(true, false, true, Key.F10)
	var shortcutRunAsScript = ShortCut(true, false, false, Key.R)
	var shortcutNextLine = ShortCut(false, false, true, Key.ENTER)
	var shortcutSplitLine = ShortCut(true, false, false, Key.ENTER)
	var shortcutNewLineBefore = ShortCut(true, true, false, Key.ENTER)
	var shortcutComment = ShortCut(true, false, false, Key.SLASH)
	var shortcutBlockComment = ShortCut(true, false, true, Key.SLASH)
	var shortcutFind = ShortCut(true, false, false, Key.F)
	var shortcutReplace = ShortCut(true, false, false, Key.R)

	private fun defaultOf(name: String, value: String) {
		if (!properties.containsKey(name)) properties[name] = value
	}

	private fun initImageProperty(property: KMutableProperty<Pair<String, BufferedImage?>>) {
		ignoreException {
			properties[property.name]?.toString()?.also {
				property.setter.call(it to ImageIO.read(File(it)))
			}
		}
	}

	private fun initIntProperty(property: KMutableProperty<Int>) {
		handleException {
			properties[property.name]?.toString()?.also {
				it.toIntOrNull()?.let { property.setter.call(it) }
			}
		}
	}

	private fun initShortCutProperty(property: KMutableProperty<ShortCut>) {
		properties[property.name]?.toString()?.also {
			ShortCut.valueOf(it)?.let { property.setter.call(it) }
		}
	}

	fun loadFile(file: File) = properties.load(file.inputStream())

	fun load() {
		if (!configFile.exists()) configFile.createNewFile()
		else loadFile(configFile)
		defaultOf(::lastOpenedFile.name, "")
		defaultOf(::javaClassName.name, "DevKtCompiled")
		defaultOf(::jarName.name, "DevKtCompiled.jar")
		defaultOf(::appName.name, "Dev Kt")
		defaultOf(::monoFontName.name, defaultFontName)
		defaultOf(::gothicFontName.name, defaultFontName)
		defaultOf(::colorDefault.name, "#A9B7C6")
		defaultOf(::colorKeywords.name, "#CC7832")
		defaultOf(::colorPredefined.name, "#507874")
		defaultOf(::colorString.name, "#6A8759")
		defaultOf(::colorStringEscape.name, "#CC7832")
		defaultOf(::colorInterpolation.name, "#CC7832")
		defaultOf(::colorTemplateEntries.name, "#CC7832")
		defaultOf(::colorCharLiteral.name, "#6A8759")
		defaultOf(::colorLineComments.name, "#808080")
		defaultOf(::colorBlockComments.name, "#808080")
		defaultOf(::colorDocComments.name, "#629755")
		defaultOf(::colorOperators.name, "#A9B7C6")
		defaultOf(::colorParentheses.name, "#A9B7C6")
		defaultOf(::colorBraces.name, "#A9B7C6")
		defaultOf(::colorBrackets.name, "#A9B7C6")
		defaultOf(::colorSemicolon.name, "#CC7832")
		defaultOf(::colorNumbers.name, "#6897BB")
		defaultOf(::colorIdentifiers.name, "#A9B7C6")
		defaultOf(::colorAnnotations.name, "#BBB529")
		defaultOf(::colorColon.name, "#A9B7C6")
		defaultOf(::colorComma.name, "#CC7832")
		defaultOf(::colorVariable.name, "#BCA5C4")
		defaultOf(::colorFunction.name, "#FFC66D")
		defaultOf(::colorUnknown.name, "#BC3F3C")
		defaultOf(::colorError.name, "#BC3F3C")
		defaultOf(::colorTypeParam.name, "#6897BB")
		defaultOf(::colorUserTypeRef.name, "#B5B6E3")
		defaultOf(::colorProperty.name, "#9876AA")
		defaultOf(::colorNamespace.name, "#6D9CBE")
		defaultOf(::colorMetaData.name, "#BBB529")
		defaultOf(::colorMacro.name, "#4EADE5")
		defaultOf(::colorBackground.name, "#2B2B2B")
		defaultOf(::colorInputError.name, "#743A39")
		initImageProperty(::windowIcon)
		initImageProperty(::backgroundImage)
		properties[::windowBounds.name]
				?.toString()
				?.split(',', limit = 4)
				?.also { (x, y, width, height) ->
					x.toIntOrNull()?.let { windowBounds.x = it }
					y.toIntOrNull()?.let { windowBounds.y = it }
					width.toIntOrNull()?.let { windowBounds.width = it }
					height.toIntOrNull()?.let { windowBounds.height = it }
				}
		initIntProperty(::tabSize)
		initIntProperty(::psiViewerMaxCodeLength)
		initIntProperty(::backgroundAlpha)
		properties[::fontSize.name]?.toString()?.toFloatOrNull()?.let { fontSize = it }
		properties[::useTab.name]?.let { useTab = it.toString() == "true" }
		properties[::highlightTokenBased.name]?.let { highlightTokenBased = it.toString() == "true" }
		properties[::highlightSemanticBased.name]?.let { highlightSemanticBased = it.toString() == "true" }
		properties[::recentFiles.name]?.run {
			toString()
					.split(File.pathSeparatorChar)
					.mapNotNullTo(recentFiles) { File(it).takeIf { it.exists() } }
		}

		initShortCutProperty(::shortcutUndo)
		initShortCutProperty(::shortcutRedo)
		initShortCutProperty(::shortcutOpen)
		initShortCutProperty(::shortcutSave)
		initShortCutProperty(::shortcutSync)
		initShortCutProperty(::shortcutGoto)

		initShortCutProperty(::shortcutNextLine)
		initShortCutProperty(::shortcutSplitLine)
		initShortCutProperty(::shortcutNewLineBefore)
	}

	fun save() {
		properties[::recentFiles.name] = recentFiles.joinToString(File.pathSeparator)
		properties[::useTab.name] = useTab.toString()
		properties[::fontSize.name] = fontSize.toString()
		properties[::tabSize.name] = tabSize.toString()
		properties[::psiViewerMaxCodeLength.name] = psiViewerMaxCodeLength.toString()
		properties[::backgroundAlpha.name] = backgroundAlpha.toString()
		properties[::highlightTokenBased.name] = highlightTokenBased.toString()
		properties[::highlightSemanticBased.name] = highlightSemanticBased.toString()
		properties[::windowBounds.name] = "${windowBounds.x},${windowBounds.y},${windowBounds.width},${windowBounds.height}"
		properties[::windowIcon.name] = windowIcon.first
		properties[::backgroundImage.name] = backgroundImage.first
		properties[::shortcutUndo.name] = shortcutUndo.toString()
		properties[::shortcutOpen.name] = shortcutOpen.toString()
		properties[::shortcutRedo.name] = shortcutRedo.toString()
		properties[::shortcutSave.name] = shortcutSave.toString()
		properties[::shortcutSync.name] = shortcutSync.toString()
		properties[::shortcutGoto.name] = shortcutGoto.toString()
		properties[::shortcutNextLine.name] = shortcutNextLine.toString()
		properties[::shortcutSplitLine.name] = shortcutSplitLine.toString()
		properties[::shortcutNewLineBefore.name] = shortcutNewLineBefore.toString()
		properties.store(configFile.outputStream(), null)
	}
}
