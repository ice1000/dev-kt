package org.ice1000.devkt.ui.jfx

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import org.ice1000.devkt.ui.MessageType
import org.ice1000.devkt.ui.UIBase
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import java.io.File

/**
 * @param T Unknown ATM
 */
abstract class AbstractJfxUI<T>(private val application: Application) : UIBase<T>() {
	private fun <T> initAlert(it: Dialog<T>, title: String) {
		it.isResizable = false
		it.title = title
		it.showAndWait()
	}

	override fun dialogYesNo(text: String, messageType: MessageType, title: String): Boolean =
			Alert(when (messageType) {
				MessageType.Error -> Alert.AlertType.ERROR
				MessageType.Information -> Alert.AlertType.INFORMATION
				MessageType.Plain -> Alert.AlertType.NONE
				MessageType.Question -> Alert.AlertType.CONFIRMATION
				MessageType.Warning -> Alert.AlertType.WARNING
			}, text, ButtonType.YES, ButtonType.NO).let {
				initAlert(it, title)
				return@let it.result == ButtonType.YES
			}

	override fun dialog(text: String, messageType: MessageType, title: String) {
		Alert(when (messageType) {
			MessageType.Error -> Alert.AlertType.ERROR
			MessageType.Information -> Alert.AlertType.INFORMATION
			MessageType.Plain -> Alert.AlertType.NONE
			MessageType.Question -> Alert.AlertType.CONFIRMATION
			MessageType.Warning -> Alert.AlertType.WARNING
		}, text, ButtonType.OK).let { initAlert(it, title) }
	}

	override fun doOpen(file: File) = doBrowse(file.toURI().toString())
	override fun doBrowse(url: String) =
			if (SystemInfo.isOracleJvm)
				application.hostServices.showDocument(url)
			else throw UnsupportedOperationException(
					"browsing files cannot be done on non-oracle jvm.")
}
