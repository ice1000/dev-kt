package org.ice1000.devkt

data class Quad<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

val paired = mapOf(
		"\"" to "\"",
		"'" to "'",
		"(" to ")",
		"（" to "）",
		"『" to "』",
		"「" to "」",
		"〖" to "〗",
		"【" to "】",
		"[" to "]",
		"〔" to "〕",
		"［" to "］",
		"{" to "}",
		"｛" to "｝",
		"<" to ">",
		"《" to "》",
		"〈" to "〉",
		"‹" to "›",
		"«" to "»"
)

