package com.github.wpm.lean.editor

import com.github.wpm.lean.lexer.LeanTokenTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class LeanBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            BracePair(LeanTokenTypes.LPAREN, LeanTokenTypes.RPAREN, false),
            BracePair(LeanTokenTypes.LBRACKET, LeanTokenTypes.RBRACKET, false),
            BracePair(LeanTokenTypes.LBRACE, LeanTokenTypes.RBRACE, false),
            BracePair(LeanTokenTypes.LANGLE, LeanTokenTypes.RANGLE, false),
            BracePair(LeanTokenTypes.LDBRACE, LeanTokenTypes.RDBRACE, false),
        )
    }
}
