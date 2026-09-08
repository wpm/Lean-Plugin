package com.github.wpm.lean.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/** Adapts the platform-independent [LeanTokenizer] to the IntelliJ [com.intellij.lexer.Lexer] contract. */
class LeanLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var bufferEnd = 0
    private var tokenizer: LeanTokenizer? = null
    private var token: LeanToken? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferEnd = endOffset
        tokenizer = LeanTokenizer(buffer, startOffset, endOffset)
        token = tokenizer?.next()
    }

    /** Every token boundary is a valid restart point, so the lexer is state-free. */
    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = token?.let { LeanTokenTypes.fromKind(it.kind) }

    override fun getTokenStart(): Int = token?.start ?: bufferEnd

    override fun getTokenEnd(): Int = token?.end ?: bufferEnd

    override fun advance() {
        token = tokenizer?.next()
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd
}
