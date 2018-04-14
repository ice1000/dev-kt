package org.ice1000.devkt.config

import org.ice1000.devkt.`{-# LANGUAGE SarasaGothicFont #-}`.defaultFontName
import org.ice1000.devkt.handleException
import org.ice1000.devkt.ignoreException
import org.ice1000.devkt.lie.ctrlOrMeta
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.reflect.KMutableProperty

class ShortCut {
	val isControl: Boolean
	val isAlt: Boolean
	val isShift: Boolean
	val keyCode: Int

	val modifier: Int

	constructor(isControl: Boolean, isAlt: Boolean, isShift: Boolean, keyCode: Int) {
		this.isControl = isControl
		this.isAlt = isAlt
		this.isShift = isShift
		this.keyCode = keyCode
		this.modifier = (if (isControl) ctrlOrMeta else 0) or
				(if (isAlt) KeyEvent.ALT_DOWN_MASK else 0) or
				(if (isShift) KeyEvent.SHIFT_DOWN_MASK else 0)
	}

	/**
	 * @param modifier Int
	 * @param keyCode Int
	 * @constructor use this constructor to prevent `ctrl` being transformed into `meta` in Mac
	 */
	constructor(modifier: Int, keyCode: Int) {
		this.modifier = modifier
		this.keyCode = keyCode
		this.isControl = modifier and KeyEvent.CTRL_DOWN_MASK != 0
		this.isShift = modifier and KeyEvent.SHIFT_DOWN_MASK != 0
		this.isAlt = modifier and KeyEvent.ALT_DOWN_MASK != 0
	}

	companion object {
		fun parse(str: String): ShortCut? {
			str.split("|").run {
				val keyCode = firstOrNull()?.toIntOrNull() ?: return null
				val modifier = getOrNull(1)?.toIntOrNull() ?: return null
				return ShortCut(modifier, keyCode)
			}
		}
	}

	fun check(e: KeyEvent) = e.modifiers == modifier

	override fun toString(): String = "$keyCode|$modifier"
}

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
	var recentFiles = hashSetOf<File>()

	var javaClassName: String by properties
	var jarName: String by properties
	var appName: String by properties
	var monoFontName: String by properties
	var gothicFontName: String by properties
	var colorKeywords: String by properties
	var colorPredefined: String by properties
	var colorString: String by properties
	var colorStringEscape: String by properties
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
	var colorTypeParam: String by properties
	var colorUserTypeRef: String by properties
	var colorProperty: String by properties
	var colorNamespace: String by properties
	var colorMetaData: String by properties
	var colorMacro: String by properties
	var colorBackground: String by properties
	var colorInputError: String by properties

	var shortcutUndo = ShortCut(true, false, false, KeyEvent.VK_Z)
	var shortcutSave = ShortCut(true, false, false, KeyEvent.VK_S)
	var shortcutRedo = ShortCut(true, false, true, KeyEvent.VK_Z)
	var shortcutSync = ShortCut(true, true, false, KeyEvent.VK_Y)
	var shortcutGoto = ShortCut(true, false, false, KeyEvent.VK_G)
	var shortcutOpen = ShortCut(true, false, false, KeyEvent.VK_O)
	// Build | Run As | Class, force to use ctrl + R even though it is in Mac.
	var shortcutBuildRunAsClass = ShortCut(KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_R)    // Mac
	var shortcutNextLine = ShortCut(false, false, true, KeyEvent.VK_ENTER)
	var shortcutSplitLine = ShortCut(true, false, false, KeyEvent.VK_ENTER)
	var shortcutNewLineBefore = ShortCut(true, true, false, KeyEvent.VK_ENTER)
	var shortcutComment = ShortCut(true, false, false, KeyEvent.VK_SLASH)
	var shortcutFind = ShortCut(true, false, false, KeyEvent.VK_F)
	var shortcutReplace = ShortCut(true, false, false, KeyEvent.VK_R)

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
			ShortCut.parse(it)?.let { property.setter.call(it) }
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
		defaultOf(::colorKeywords.name, "#CC7832")
		defaultOf(::colorPredefined.name, "#507874")
		defaultOf(::colorString.name, "#6A8759")
		defaultOf(::colorStringEscape.name, "#CC7832")
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
