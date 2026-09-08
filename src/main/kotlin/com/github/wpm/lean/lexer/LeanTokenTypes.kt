package com.github.wpm.lean.lexer

import com.github.wpm.lean.LeanLanguage
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class LeanTokenType(debugName: String) : IElementType(debugName, LeanLanguage)

object LeanTokenTypes {
    @JvmField val LINE_COMMENT: IElementType = LeanTokenType("LINE_COMMENT")
    @JvmField val BLOCK_COMMENT: IElementType = LeanTokenType("BLOCK_COMMENT")
    @JvmField val DOC_COMMENT: IElementType = LeanTokenType("DOC_COMMENT")
    @JvmField val MODULE_DOC_COMMENT: IElementType = LeanTokenType("MODULE_DOC_COMMENT")
    @JvmField val KEYWORD: IElementType = LeanTokenType("KEYWORD")
    @JvmField val MODIFIER: IElementType = LeanTokenType("MODIFIER")
    @JvmField val COMMAND: IElementType = LeanTokenType("COMMAND")
    @JvmField val SORT: IElementType = LeanTokenType("SORT")
    @JvmField val SORRY: IElementType = LeanTokenType("SORRY")
    @JvmField val BOOLEAN: IElementType = LeanTokenType("BOOLEAN")
    @JvmField val IDENTIFIER: IElementType = LeanTokenType("IDENTIFIER")
    @JvmField val NUMBER: IElementType = LeanTokenType("NUMBER")
    @JvmField val STRING: IElementType = LeanTokenType("STRING")
    @JvmField val CHAR: IElementType = LeanTokenType("CHAR")
    @JvmField val LPAREN: IElementType = LeanTokenType("LPAREN")
    @JvmField val RPAREN: IElementType = LeanTokenType("RPAREN")
    @JvmField val LBRACKET: IElementType = LeanTokenType("LBRACKET")
    @JvmField val RBRACKET: IElementType = LeanTokenType("RBRACKET")
    @JvmField val LBRACE: IElementType = LeanTokenType("LBRACE")
    @JvmField val RBRACE: IElementType = LeanTokenType("RBRACE")
    @JvmField val LANGLE: IElementType = LeanTokenType("LANGLE")
    @JvmField val RANGLE: IElementType = LeanTokenType("RANGLE")
    @JvmField val LDBRACE: IElementType = LeanTokenType("LDBRACE")
    @JvmField val RDBRACE: IElementType = LeanTokenType("RDBRACE")
    @JvmField val SYMBOL: IElementType = LeanTokenType("SYMBOL")

    @JvmField val COMMENTS: TokenSet = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT, DOC_COMMENT, MODULE_DOC_COMMENT)
    @JvmField val STRINGS: TokenSet = TokenSet.create(STRING, CHAR)

    fun fromKind(kind: LeanTokenKind): IElementType = when (kind) {
        LeanTokenKind.WHITESPACE -> TokenType.WHITE_SPACE
        LeanTokenKind.LINE_COMMENT -> LINE_COMMENT
        LeanTokenKind.BLOCK_COMMENT -> BLOCK_COMMENT
        LeanTokenKind.DOC_COMMENT -> DOC_COMMENT
        LeanTokenKind.MODULE_DOC_COMMENT -> MODULE_DOC_COMMENT
        LeanTokenKind.KEYWORD -> KEYWORD
        LeanTokenKind.MODIFIER -> MODIFIER
        LeanTokenKind.COMMAND -> COMMAND
        LeanTokenKind.SORT -> SORT
        LeanTokenKind.SORRY -> SORRY
        LeanTokenKind.BOOLEAN -> BOOLEAN
        LeanTokenKind.IDENTIFIER -> IDENTIFIER
        LeanTokenKind.NUMBER -> NUMBER
        LeanTokenKind.STRING -> STRING
        LeanTokenKind.CHAR -> CHAR
        LeanTokenKind.LPAREN -> LPAREN
        LeanTokenKind.RPAREN -> RPAREN
        LeanTokenKind.LBRACKET -> LBRACKET
        LeanTokenKind.RBRACKET -> RBRACKET
        LeanTokenKind.LBRACE -> LBRACE
        LeanTokenKind.RBRACE -> RBRACE
        LeanTokenKind.LANGLE -> LANGLE
        LeanTokenKind.RANGLE -> RANGLE
        LeanTokenKind.LDBRACE -> LDBRACE
        LeanTokenKind.RDBRACE -> RDBRACE
        LeanTokenKind.SYMBOL -> SYMBOL
        LeanTokenKind.BAD -> TokenType.BAD_CHARACTER
    }
}
