package org.jetbrains.kotlin.com.intellij.lexer

import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

open class DelegateLexer(private val delegate: Lexer) : LexerBase() {
	override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) =
			this.delegate.start(buffer, startOffset, endOffset, initialState)

	override fun getState(): Int = this.delegate.state
	override fun getTokenType(): IElementType? = this.delegate.tokenType
	override fun getTokenStart(): Int = this.delegate.tokenStart
	override fun getTokenEnd(): Int = this.delegate.tokenEnd
	override fun advance() = this.delegate.advance()
	override fun getBufferSequence(): CharSequence = this.delegate.bufferSequence
	override fun getBufferEnd(): Int = this.delegate.bufferEnd
}
