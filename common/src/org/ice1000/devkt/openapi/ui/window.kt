package org.ice1000.devkt.openapi.ui

import org.ice1000.devkt.openapi.util.CompletionElement
import org.ice1000.devkt.openapi.util.CompletionPopup
import org.ice1000.devkt.ui.ChooseFileType
import org.ice1000.devkt.ui.MessageType
import java.io.File

interface DevKtWindow {
	var edited: Boolean
	fun refreshTitle()
	fun uiThread(lambda: () -> Unit)
	fun message(text: String)
	fun createCompletionPopup(completionList: Collection<CompletionElement>): CompletionPopup
	fun chooseFile(from: File?, chooseFileType: ChooseFileType): File?
	fun chooseDir(from: File?, chooseFileType: ChooseFileType): File?
	fun dialogYesNo(
			text: String,
			messageType: MessageType = MessageType.Information,
			title: String = messageType.name): Boolean

	fun dialog(
			text: String,
			messageType: MessageType = MessageType.Information,
			title: String = messageType.name)

	fun loadFile(it: File)
	fun sync()
	fun exit()
	fun restart()
}
