package org.ice1000.devkt.ui.jfx

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import org.ice1000.devkt.ui.MessageType
import org.ice1000.devkt.ui.UIBase

/**
 * @param T Unknown ATM
 */
abstract class AbstractJfxUI<T> : UIBase<T>() {
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
}
