package org.ice1000.devkt.psi

import org.ice1000.devkt.config.ColorScheme
import org.ice1000.devkt.ui.AnnotationHolder
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

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
		if (element.nodeType in KtTokens.SOFT_KEYWORDS) {
			document.highlight(element, colorScheme.keywords)
			return
		}
		when (element) {
			is KtAnnotationEntry -> annotationEntry(element, document, colorScheme)
			is KtTypeParameter -> typeParameter(element, document, colorScheme)
			is KtTypeReference -> typeReference(element, document, colorScheme)
			is KtNamedFunction -> namedFunction(element, document, colorScheme)
		}
	}

	private fun typeReference(
			element: KtTypeReference, document: AnnotationHolder, colorScheme: ColorScheme) {
		if (element.parent !is KtConstructorCalleeExpression)
			document.highlight(element.firstChild
					?.takeIf { it is KtUserType || it is KtNullableType }
					?: return, colorScheme.userTypeRef)
	}

	private fun namedFunction(
			element: KtNamedFunction, document: AnnotationHolder, colorScheme: ColorScheme) {
		element.nameIdentifier?.let {
			document.highlight(it, colorScheme.function)
		}
	}

	private fun typeParameter(
			element: KtTypeParameter, document: AnnotationHolder, colorScheme: ColorScheme) {
		document.highlight(element, colorScheme.typeParam)
		element.references.forEach {
			val refTo = it.element ?: return@forEach
			document.highlight(refTo, colorScheme.typeParam)
		}
	}

	private fun annotationEntry(
			element: KtAnnotationEntry, document: AnnotationHolder, colorScheme: ColorScheme) {
		val start = element.startOffset
		val end = element.typeReference?.endOffset ?: start
		document.highlight(start, end, colorScheme.annotations)
	}
}
