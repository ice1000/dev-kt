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
	val searchResult: MutableList<SearchResult>
	var currentIndex: Int

	fun search(bundle: FindDataBundle)
	fun select(index: Int)
	fun moveUp()
	fun moveDown()
}

interface IReplace {
	fun replaceCurrent(bundle: FindDataBundle)
	fun replaceAll(bundle: FindDataBundle)
}

open class AbstractFindDialog(val document: DevKtDocumentHandler<*>) : IFind {
	companion object {
		val NO_REGEXP_CHARS = "\\{[(+*^\$.?|".toCharArray()
	}

	override val searchResult = arrayListOf<SearchResult>()
	override var currentIndex = 0

	override fun search(bundle: FindDataBundle) {
		searchResult.clear()
		document.selectionEnd = document.selectionStart

		val input = bundle.findInput
		val text = document.text
		val regex = if (bundle.isRegex.not()) NO_REGEXP_CHARS.fold(input) { last, current ->
			last.replace(current.toString(), "\\$current")
		} else input

		try {
			Pattern.compile(
					regex,
					if (bundle.isMatchCase.not()) Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE else 0
			).matcher(text).run {
				while (find()) {
					searchResult.add(SearchResult(start(), end()))
				}
			}

			select(0)
		} catch (e: PatternSyntaxException) {
			//TODO 做出提示
		}
	}

	override fun select(index: Int) {
		searchResult.getOrNull(index)?.let { (start, end) ->
			currentIndex = index
			document.selectionStart = start
			document.selectionEnd = end
		}
	}

	override fun moveUp() = select(currentIndex - 1)
	override fun moveDown() = select(currentIndex + 1)
}

class AbstractReplaceDialog(document: DevKtDocumentHandler<*>) : AbstractFindDialog(document), IReplace {
	override fun replaceCurrent(bundle: FindDataBundle) {
		searchResult.getOrNull(currentIndex)?.run {
			document.resetTextTo(document.text.replaceRange(start until end, bundle.replaceInput ?: return))
		}
	}

	override fun replaceAll(bundle: FindDataBundle) {
		val findInput = bundle.findInput
		val replaceInput = bundle.replaceInput ?: return
		document.resetTextTo(if (bundle.isRegex) {
			document.replaceText(Regex(findInput), replaceInput)
		} else document.text.replace(findInput, replaceInput))
	}
}