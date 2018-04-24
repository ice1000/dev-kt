package org.ice1000.devkt.ui

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

data class SearchResult(val start: Int, val end: Int)

data class FindDataBundle(
		var findInput: String,
		var isMatchCase: Boolean,
		var isRegex: Boolean,
		var replaceInput: String? = null
)

interface IFind {
	companion object {
		val NO_REGEXP_CHARS = "\\{[(+*^\$.?|".toCharArray()
	}

	val searchResult: MutableList<SearchResult>
	var currentIndex: Int
	val document: DevKtDocumentHandler<*>

	@JvmDefault
	fun search(bundle: FindDataBundle) {
		searchResult.clear()
		currentIndex = 0
		document.selectionEnd = document.selectionStart

		if (bundle.findInput.isEmpty()) {
			update()
			return
		}

		val input = bundle.findInput
		val text = document.text
		val regex = if (!bundle.isRegex) NO_REGEXP_CHARS.fold(input) { last, current ->
			last.replace(current.toString(), "\\$current")
		} else input

		try {
			Pattern.compile(
					regex,
					if (bundle.isMatchCase.not()) Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE else 0
			).matcher(text).run {
				while (find()) searchResult += SearchResult(start(), end())
			}
			select(0)
		} catch (e: PatternSyntaxException) {
			//TODO 做出提示
		}
	}

	@JvmDefault
	fun select(index: Int) {
		searchResult.getOrNull(index)?.let { (start, end) ->
			currentIndex = index
			document.selectionStart = start
			document.selectionEnd = end

		}

		update()
	}

	@JvmDefault
	fun moveUp() = select(currentIndex - 1)

	@JvmDefault
	fun moveDown() = select(currentIndex + 1)

	fun update()
}

interface IReplace : IFind {
	@JvmDefault
	fun replaceCurrent(bundle: FindDataBundle) {
		searchResult.getOrNull(currentIndex)?.run {
			document.resetTextTo(document.text.replaceRange(start until end, bundle.replaceInput ?: return))
		}
	}

	@JvmDefault
	fun replaceAll(bundle: FindDataBundle) {
		val findInput = bundle.findInput
		val replaceInput = bundle.replaceInput ?: return
		document.resetTextTo(if (bundle.isRegex) {
			document.replaceText(Regex(findInput), replaceInput)
		} else document.text.replace(findInput, replaceInput))
	}
}
