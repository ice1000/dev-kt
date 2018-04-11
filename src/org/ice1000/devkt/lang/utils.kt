package org.ice1000.devkt.lang

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

val PsiElement.nodeType: IElementType get() = node.elementType

fun cutText(string: String, max: Int) =
		if (string.length <= max) string else "${string.take(max)}…"
