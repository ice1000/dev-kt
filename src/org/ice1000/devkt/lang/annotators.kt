package org.ice1000.devkt.lang

import org.ice1000.devkt.openapi.*
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * @author ice1000
 * @since v1.2
 */
class JavaAnnotator<TextAttributes> : Annotator<TextAttributes> {
	override fun annotate(
			element: PsiElement,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		when (element) {
			is PsiAnnotation -> annotation(element, document, colorScheme)
			is PsiTypeElement -> typeElement(element, document, colorScheme)
			is PsiMethod -> method(element, document, colorScheme)
			is PsiField -> field(element, document, colorScheme)
			is PsiVariable -> variable(element, document, colorScheme)
			is PsiErrorElement -> document.highlight(element, colorScheme.unknown)
		}
	}

	private fun variable(
			element: PsiVariable,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		element.nameIdentifier?.let { document.highlight(it, colorScheme.variable) }
	}

	private fun method(
			element: PsiMethod,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		element.nameIdentifier?.let { document.highlight(it, colorScheme.function) }
	}

	private fun field(
			element: PsiField,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		document.highlight(element.nameIdentifier, colorScheme.property)
	}

	private fun typeElement(
			element: PsiElement,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		if (element.firstChild !is PsiKeyword)
			document.highlight(element, colorScheme.userTypeRef)
	}

	private fun annotation(
			element: PsiAnnotation,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		val start = element.startOffset
		val end = element.nameReferenceElement?.endOffset ?: element.firstChild?.endOffset ?: start
		document.highlight(start, end, colorScheme.annotations)
	}
}

/**
 * @author ice1000
 * @since v0.0.1
 */
class KotlinAnnotator<TextAttributes> : Annotator<TextAttributes> {
	override fun annotate(
			element: PsiElement,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		if (element.nodeType in KtTokens.SOFT_KEYWORDS) {
			document.highlight(element, colorScheme.keywords)
			return
		}
		when (element) {
			is KtAnnotationEntry -> annotationEntry(element, document, colorScheme)
			is KtTypeParameter -> typeParameter(element, document, colorScheme)
			is KtTypeReference -> typeReference(element, document, colorScheme)
			is KtNamedFunction -> namedFunction(element, document, colorScheme)
			is KtProperty -> property(element, document, colorScheme)
			is PsiErrorElement -> document.highlight(element, colorScheme.unknown)
		}
	}

	private fun property(
			element: KtProperty,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		element.nameIdentifier?.let {
			document.highlight(it, colorScheme.property)
		}
	}

	private fun typeReference(
			element: KtTypeReference,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		if (element.parent !is KtConstructorCalleeExpression)
			document.highlight(element.firstChild
					?.takeIf { it is KtUserType || it is KtNullableType }
					?: return, colorScheme.userTypeRef)
	}

	private fun namedFunction(
			element: KtNamedFunction,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		element.nameIdentifier?.let {
			document.highlight(it, colorScheme.function)
		}
	}

	private fun typeParameter(
			element: KtTypeParameter,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		document.highlight(element, colorScheme.typeParam)
		element.references.forEach {
			val refTo = it.element ?: return@forEach
			document.highlight(refTo, colorScheme.typeParam)
		}
	}

	private fun annotationEntry(
			element: KtAnnotationEntry,
			document: AnnotationHolder<TextAttributes>,
			colorScheme: ColorScheme<TextAttributes>) {
		val start = element.startOffset
		val end = element.typeReference?.endOffset ?: element.atSymbol?.endOffset ?: start
		document.highlight(start, end, colorScheme.annotations)
	}
}


