package org.ice1000.devkt.openapi.util

import org.ice1000.devkt.openapi.ui.IDevKtDocumentHandler
import org.ice1000.devkt.ui.DevKtDocumentHandler
import java.util.function.Consumer
import javax.swing.Icon

/**
 * Builder? No need!
 *
 * @author ice1000
 * @property text String see [com.intellij.codeInsight.lookup.LookupElementBuilder.create]
 * @property lookup String see [com.intellij.codeInsight.lookup.LookupElementBuilder.withLookupString]
 * @property tail String see [com.intellij.codeInsight.lookup.LookupElementBuilder.withTailText]
 * @property type String see [com.intellij.codeInsight.lookup.LookupElementBuilder.withTypeText]
 * @property icon Icon see [com.intellij.codeInsight.lookup.LookupElementBuilder.withIcon]
 * @since v1.4
 */
open class CompletionElement
@JvmOverloads
constructor(
		val text: Any = "",
		val lookup: String = text.toString(),
		val tail: String = "",
		val type: String = "",
		val icon: Icon? = null
) {
	override fun toString() = lookup
	override fun hashCode() = text.hashCode()
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is CompletionElement) return false
		if (text != other.text) return false
		return true
	}

	open fun afterInsert(documentHandler: DevKtDocumentHandler<*>) = Unit
}

/**
 * @author ice1000
 * @since v1.4
 */
interface CompletionPopup {
	fun show()
	fun hide()
	fun updateItems(completionElement: Collection<CompletionElement>)
}
