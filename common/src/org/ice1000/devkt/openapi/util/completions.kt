package org.ice1000.devkt.openapi.util

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
data class CompletionElement
@JvmOverloads
constructor(
		val text: String = "",
		val lookup: String = text,
		val tail: String = "",
		val type: String = "",
		val icon: Icon? = null
)

/**
 * @author ice1000
 * @since v1.4
 */
interface CompletionPopup {
	fun show()
	fun hide()
}
