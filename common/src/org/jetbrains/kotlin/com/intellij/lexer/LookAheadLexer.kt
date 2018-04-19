/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
@file:Suppress("MemberVisibilityCanBePrivate")

package org.jetbrains.kotlin.com.intellij.lexer;

import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.util.containers.IdeaQueue
import org.jetbrains.kotlin.com.intellij.util.containers.ImmutableUserMap

/**
 * @author peter
 */
abstract class LookAheadLexer @JvmOverloads constructor(private val myBaseLexer: Lexer, capacity: Int = 64) : LexerBase() {
	private var myLastOffset: Int = 0
	private var myLastState: Int = 0
	private var myTokenStart: Int = 0
	private val myTypeCache: IdeaQueue<IElementType?> = IdeaQueue(capacity)
	private val myEndOffsetCache: IdeaQueue<Int> = IdeaQueue(capacity)

	protected open val cacheSize: Int
		get() = myTypeCache.size()

	protected open fun addToken(type: IElementType?) {
		addToken(myBaseLexer.tokenEnd, type)
	}

	protected open fun addToken(endOffset: Int, type: IElementType?) {
		myTypeCache.addLast(type)
		myEndOffsetCache.addLast(endOffset)
	}

	protected open fun lookAhead(baseLexer: Lexer) {
		advanceLexer(baseLexer)
	}

	override fun advance() {
		if (!myTypeCache.isEmpty) {
			myTypeCache.pullFirst()
			myTokenStart = myEndOffsetCache.pullFirst()
		}
		if (myTypeCache.isEmpty) doLookAhead()
	}

	private fun doLookAhead() {
		myLastOffset = myTokenStart
		myLastState = myBaseLexer.state

		lookAhead(myBaseLexer)
		assert(!myTypeCache.isEmpty)
	}

	override fun getBufferSequence(): CharSequence {
		return myBaseLexer.bufferSequence
	}

	override fun getBufferEnd(): Int {
		return myBaseLexer.bufferEnd
	}

	protected open fun resetCacheSize(size: Int) {
		while (myTypeCache.size() > size) {
			myTypeCache.removeLast()
			myEndOffsetCache.removeLast()
		}
	}

	open fun replaceCachedType(index: Int, token: IElementType): IElementType? {
		return myTypeCache.set(index, token)
	}

	protected open fun getCachedType(index: Int): IElementType? {
		return myTypeCache[index]
	}

	protected open fun getCachedOffset(index: Int): Int {
		return myEndOffsetCache[index]
	}

	override fun getState(): Int {
		val offset = myTokenStart - myLastOffset
		return myLastState or (offset shl 16)
	}

	override fun getTokenEnd(): Int {
		return myEndOffsetCache.peekFirst()
	}

	override fun getTokenStart(): Int {
		return myTokenStart
	}

	override fun getCurrentPosition(): LookAheadLexerPosition {
		return LookAheadLexerPosition(this, ImmutableUserMap.EMPTY)
	}

	override fun restore(position: LexerPosition) {
		restore(position as LookAheadLexerPosition)
	}

	protected open fun restore(position: LookAheadLexerPosition) {
		start(myBaseLexer.bufferSequence, position.lastOffset, myBaseLexer.bufferEnd, position.lastState)
		for (i in 0 until position.advanceCount) {
			advance()
		}
	}

	override fun getTokenType() = myTypeCache.peekFirst()

	override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
		myBaseLexer.start(buffer, startOffset, endOffset, initialState and 0xFFFF)
		myTokenStart = startOffset
		myTypeCache.clear()
		myEndOffsetCache.clear()
		doLookAhead()
	}

	class LookAheadLexerPosition(lookAheadLexer: LookAheadLexer, val customMap: ImmutableUserMap) : LexerPosition {
		internal val lastOffset: Int = lookAheadLexer.myLastOffset
		internal val lastState: Int = lookAheadLexer.myLastState
		internal val tokenStart: Int = lookAheadLexer.myTokenStart
		internal val advanceCount: Int = lookAheadLexer.myTypeCache.size() - 1

		override fun getOffset() = tokenStart
		override fun getState() = lastState
	}

	protected open fun advanceLexer(lexer: Lexer) {
		advanceAs(lexer, lexer.tokenType)
	}

	protected open fun advanceAs(lexer: Lexer, type: IElementType?) {
		addToken(type)
		lexer.advance()
	}

}
