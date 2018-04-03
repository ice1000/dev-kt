package org.ice1000.devkt.ui

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtAnnotation

/**
 * @author ice1000
 * @since v0.0.1
 * @see com.intellij.lang.annotation.Annotator
 */
class KotlinAnnotator {
	/**
	 * @param element the [PsiElement] to be highlighted
	 * @param document similar to [com.intellij.lang.annotation.AnnotationHolder]
	 * @param colorScheme current color scheme, initialized in [org.ice1000.devkt.config.GlobalSettings]
	 */
	fun annotate(element: PsiElement, document: AnnotationHolder, colorScheme: ColorScheme) {
		when (element) {
			is KtAnnotation -> {
				document.highlight(element, colorScheme.annotations)
			}
		}
	}
}
