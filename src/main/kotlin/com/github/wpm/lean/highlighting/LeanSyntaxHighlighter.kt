package com.github.wpm.lean.highlighting

import com.github.wpm.lean.lexer.LeanLexer
import com.github.wpm.lean.lexer.LeanTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class LeanSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = LeanLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = pack(ATTRIBUTES[tokenType])

    companion object {
        private val ATTRIBUTES: Map<IElementType, TextAttributesKey> = mapOf(
            LeanTokenTypes.KEYWORD to LeanHighlighterColors.KEYWORD,
            LeanTokenTypes.MODIFIER to LeanHighlighterColors.MODIFIER,
            LeanTokenTypes.COMMAND to LeanHighlighterColors.COMMAND,
            LeanTokenTypes.SORT to LeanHighlighterColors.SORT,
            LeanTokenTypes.SORRY to LeanHighlighterColors.SORRY,
            LeanTokenTypes.BOOLEAN to LeanHighlighterColors.BOOLEAN,
            LeanTokenTypes.IDENTIFIER to LeanHighlighterColors.IDENTIFIER,
            LeanTokenTypes.NUMBER to LeanHighlighterColors.NUMBER,
            LeanTokenTypes.STRING to LeanHighlighterColors.STRING,
            LeanTokenTypes.CHAR to LeanHighlighterColors.CHAR,
            LeanTokenTypes.LINE_COMMENT to LeanHighlighterColors.LINE_COMMENT,
            LeanTokenTypes.BLOCK_COMMENT to LeanHighlighterColors.BLOCK_COMMENT,
            LeanTokenTypes.DOC_COMMENT to LeanHighlighterColors.DOC_COMMENT,
            LeanTokenTypes.MODULE_DOC_COMMENT to LeanHighlighterColors.DOC_COMMENT,
            LeanTokenTypes.LPAREN to LeanHighlighterColors.PARENTHESES,
            LeanTokenTypes.RPAREN to LeanHighlighterColors.PARENTHESES,
            LeanTokenTypes.LBRACKET to LeanHighlighterColors.BRACKETS,
            LeanTokenTypes.RBRACKET to LeanHighlighterColors.BRACKETS,
            LeanTokenTypes.LBRACE to LeanHighlighterColors.BRACES,
            LeanTokenTypes.RBRACE to LeanHighlighterColors.BRACES,
            LeanTokenTypes.LDBRACE to LeanHighlighterColors.BRACES,
            LeanTokenTypes.RDBRACE to LeanHighlighterColors.BRACES,
            LeanTokenTypes.LANGLE to LeanHighlighterColors.ANGLE_BRACKETS,
            LeanTokenTypes.RANGLE to LeanHighlighterColors.ANGLE_BRACKETS,
            LeanTokenTypes.SYMBOL to LeanHighlighterColors.OPERATOR,
            TokenType.BAD_CHARACTER to LeanHighlighterColors.BAD_CHARACTER,
        )
    }
}

class LeanSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = LeanSyntaxHighlighter()
}
