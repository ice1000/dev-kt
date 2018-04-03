package org.ice1000.devkt.ui

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry

/**
 * @author ice1000
 * @since v0.0.1
 * @see com.intellij.lang.annotation.Annotator
 * TODO move to daemon instead of running in ui thread
 */
class KotlinAnnotator {
	/**
	 * @param element the [PsiElement] to be highlighted
	 * @param document similar to [com.intellij.lang.annotation.AnnotationHolder]
	 * @param colorScheme current color scheme, initialized in [org.ice1000.devkt.config.GlobalSettings]
	 */
	fun annotate(element: PsiElement, document: AnnotationHolder, colorScheme: ColorScheme) {
		if (element.node.elementType in KtTokens.SOFT_KEYWORDS) {
			document.highlight(element, colorScheme.keywords)
			return
		}
		when (element) {
			is KtAnnotationEntry -> {
				document.highlight(element, colorScheme.annotations)
			}
		}
	}
}
