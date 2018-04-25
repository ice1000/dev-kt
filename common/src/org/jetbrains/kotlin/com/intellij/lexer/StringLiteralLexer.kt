/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.kotlin.com.intellij.lexer

import org.ice1000.devkt.openapi.util.isHex
import org.jetbrains.kotlin.com.intellij.psi.StringEscapesTokenTypes
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

/**
 * @author max
 */
@Suppress("MemberVisibilityCanBePrivate")
open class StringLiteralLexer
/**
 * @param myCanEscapeEolOrFramingSpaces true if following sequences are acceptable
 * '\' in the end of the buffer (meaning escaped end of line) or
 */
@JvmOverloads constructor(
		protected val myQuoteChar: Char,
		protected val myOriginalLiteralToken: IElementType,
		private val myCanEscapeEolOrFramingSpaces: Boolean = false,
		private val myAdditionalValidEscapes: String? = null,
		private val myAllowOctal: Boolean = true,
		private val myAllowHex: Boolean = false) : LexerBase() {

	protected lateinit var myBuffer: CharSequence
	protected var myStart: Int = 0
	protected var myEnd: Int = 0
	private var myState: Int = 0
	private var myLastState: Int = 0
	protected var myBufferEnd: Int = 0
	private var mySeenEscapedSpacesOnly: Boolean = false

	protected val unicodeEscapeSequenceType: IElementType
		get() {
			for (i in myStart + 2 until myStart + 6) {
				if (i >= myEnd || !isHex(myBuffer[i]))
					return StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
			}
			return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
		}

	override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
		myBuffer = buffer
		myStart = startOffset
		myState = if (myQuoteChar == NO_QUOTE_CHAR) AFTER_FIRST_QUOTE.toInt() else initialState
		myLastState = initialState
		myBufferEnd = endOffset
		myEnd = locateToken(myStart)
		mySeenEscapedSpacesOnly = true
	}

	override fun getState(): Int {
		return myLastState
	}

	override fun getTokenType(): IElementType? {
		if (myStart >= myEnd) return null

		if (myBuffer[myStart] != '\\') {
			mySeenEscapedSpacesOnly = false
			return myOriginalLiteralToken
		}

		if (myStart + 1 >= myEnd) {
			return handleSingleSlashEscapeSequence()
		}
		val nextChar = myBuffer[myStart + 1]
		mySeenEscapedSpacesOnly = mySeenEscapedSpacesOnly and (nextChar == ' ')
		if (myCanEscapeEolOrFramingSpaces && (nextChar == '\n' || nextChar == ' ' && (mySeenEscapedSpacesOnly || isTrailingSpace(
						myStart + 2)))) {
			return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
		}
		if (nextChar == 'u') {
			return unicodeEscapeSequenceType
		}

		if (nextChar == 'x' && myAllowHex) {
			for (i in myStart + 2 until myStart + 4) {
				if (i >= myEnd || !isHex(myBuffer[i])) return StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
			}
			return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
		}

		when (nextChar) {
			'0', '1', '2', '3', '4', '5', '6', '7' -> {
				return if (!myAllowOctal) StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN else StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
			}

			'n', 'r', 'b', 't', 'f', '\'', '\"', '\\' -> return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
		}
		return if (myAdditionalValidEscapes != null && myAdditionalValidEscapes.indexOf(nextChar) != -1) {
			StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
		} else StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN

	}

	protected fun handleSingleSlashEscapeSequence(): IElementType {
		return StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN
	}

	// all subsequent chars are escaped spaces
	private fun isTrailingSpace(start: Int): Boolean {
		var i = start
		while (i < myBufferEnd) {
			val c = myBuffer[i]
			if (c != '\\') return false
			if (i == myBufferEnd - 1) return false
			if (myBuffer[i + 1] != ' ') return false
			i += 2
		}
		return true
	}

	override fun getTokenStart() = myStart
	override fun getTokenEnd() = myEnd

	private fun locateToken(start: Int): Int {
		if (start == myBufferEnd) {
			myState = AFTER_LAST_QUOTE.toInt()
		}
		if (myState == AFTER_LAST_QUOTE.toInt()) return start
		var i = start
		if (myBuffer[i] == '\\') {
			i++
			if (i == myBufferEnd || myBuffer[i] == '\n' && !myCanEscapeEolOrFramingSpaces) {
				myState = AFTER_LAST_QUOTE.toInt()
				return i
			}

			if (myAllowOctal && myBuffer[i] >= '0' && myBuffer[i] <= '7') {
				val first = myBuffer[i]
				i++
				if (i < myBufferEnd && myBuffer[i] >= '0' && myBuffer[i] <= '7') {
					i++
					if (i < myBufferEnd && first <= '3' && myBuffer[i] >= '0' && myBuffer[i] <= '7') {
						i++
					}
				}
				return i
			}

			if (myAllowHex && myBuffer[i] == 'x') {
				return locateHexEscapeSequence(start, i)
			}

			return if (myBuffer[i] == 'u') {
				locateUnicodeEscapeSequence(start, i)
			} else {
				i + 1
			}
		}
		while (i < myBufferEnd) {
			if (myBuffer[i] == '\\') {
				return i
			}
			if (myState == AFTER_FIRST_QUOTE.toInt() && myBuffer[i] == myQuoteChar) {
				if (i + 1 == myBufferEnd) myState = AFTER_LAST_QUOTE.toInt()
				return i + 1
			}
			i++
			myState = AFTER_FIRST_QUOTE.toInt()
		}

		return i
	}

	protected fun locateHexEscapeSequence(start: Int, i: Int): Int {
		var i = i
		i++
		while (i < start + 4) {
			if (i == myBufferEnd || myBuffer[i] == '\n' || myBuffer[i] == myQuoteChar) {
				return i
			}
			i++
		}
		return i
	}

	protected fun locateUnicodeEscapeSequence(start: Int, i: Int): Int {
		var i = i
		i++
		while (i < start + 6) {
			if (i == myBufferEnd || myBuffer[i] == '\n' || myBuffer[i] == myQuoteChar) {
				return i
			}
			i++
		}
		return i
	}

	override fun advance() {
		myLastState = myState
		myStart = myEnd
		myEnd = locateToken(myStart)
	}

	override fun getBufferSequence() = myBuffer
	override fun getBufferEnd() = myBufferEnd

	override fun toString(): String {
		return "StringLiteralLexer {myAllowHex=$myAllowHex, myAllowOctal=$myAllowOctal, mySeenEscapedSpacesOnly=$mySeenEscapedSpacesOnly, myAdditionalValidEscapes='$myAdditionalValidEscapes${'\''.toString()}, myCanEscapeEolOrFramingSpaces=$myCanEscapeEolOrFramingSpaces, myOriginalLiteralToken=$myOriginalLiteralToken, myQuoteChar=$myQuoteChar, myBufferEnd=$myBufferEnd, myLastState=$myLastState, myState=$myState, myEnd=$myEnd, myStart=$myStart, myToken=" + (if (!::myBuffer.isInitialized || myEnd < myStart || myEnd > myBuffer.length)
			null
		else
			myBuffer.subSequence(
					myStart,
					myEnd)) + '}'.toString()
	}

	companion object {
		private const val AFTER_FIRST_QUOTE: Short = 1
		private const val AFTER_LAST_QUOTE: Short = 2

		const val NO_QUOTE_CHAR = (-1).toChar()
	}
}
/**
 * @param canEscapeEolOrFramingSpaces true if following sequences are acceptable
 * '\' in the end of the buffer (meaning escaped end of line) or
 * '\ ' (escaped space) in the beginning and in the end of the buffer (meaning escaped space, to avoid auto trimming on load)
 */