@file:JvmName("PsiUtils")

package org.ice1000.devkt.openapi

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

val PsiElement.nodeType: IElementType get() = node.elementType

inline fun <reified Psi : PsiElement> PsiElement.nextSiblingIgnoring(vararg types: IElementType): Psi? {
	var next: PsiElement? = nextSibling
	while (true) {
		val localNext = next ?: return null
		next = localNext.nextSibling
		return if (types.any { localNext.node.elementType == it }) continue
		else localNext as? Psi
	}
}

inline fun <reified Psi : PsiElement> PsiElement.prevSiblingIgnoring(vararg types: IElementType): Psi? {
	var next: PsiElement? = prevSibling
	while (true) {
		val localNext = next ?: return null
		next = localNext.prevSibling
		return if (types.any { localNext.node.elementType == it }) continue
		else localNext as? Psi
	}
}

fun <Psi : PsiElement> PsiElement.prevSiblingIgnoring(clazz: Class<Psi>, vararg types: IElementType): Psi? {
	var next: PsiElement? = prevSibling
	@Suppress("UNCHECKED_CAST")
	while (true) {
		val localNext = next ?: return null
		next = localNext.prevSibling
		return if (types.any { localNext.node.elementType == it }) continue
		else localNext as? Psi
	}
}

fun <Psi : PsiElement> PsiElement.nextSiblingIgnoring(clazz: Class<Psi>, vararg types: IElementType): Psi? {
	var next: PsiElement? = nextSibling
	@Suppress("UNCHECKED_CAST")
	while (true) {
		val localNext = next ?: return null
		next = localNext.nextSibling
		return if (types.any { localNext.node.elementType == it }) continue
		else localNext as? Psi
	}
}

fun PsiElement.childrenBefore(type: IElementType): List<PsiElement> {
	val ret = ArrayList<PsiElement>()
	var next: PsiElement? = firstChild
	while (true) {
		next = next?.nextSibling ?: return ret
		if (next.node.elementType == type) return ret
		ret += next
	}
}
