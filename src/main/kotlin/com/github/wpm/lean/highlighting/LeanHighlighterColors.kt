package com.github.wpm.lean.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object LeanHighlighterColors {
    @JvmField val KEYWORD: TextAttributesKey = createTextAttributesKey("LEAN_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val MODIFIER: TextAttributesKey = createTextAttributesKey("LEAN_MODIFIER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val COMMAND: TextAttributesKey = createTextAttributesKey("LEAN_COMMAND", DefaultLanguageHighlighterColors.METADATA)
    @JvmField val SORT: TextAttributesKey = createTextAttributesKey("LEAN_SORT", DefaultLanguageHighlighterColors.CLASS_NAME)
    @JvmField val SORRY: TextAttributesKey = createTextAttributesKey("LEAN_SORRY", CodeInsightColors.WARNINGS_ATTRIBUTES)
    @JvmField val BOOLEAN: TextAttributesKey = createTextAttributesKey("LEAN_BOOLEAN", DefaultLanguageHighlighterColors.CONSTANT)
    @JvmField val IDENTIFIER: TextAttributesKey = createTextAttributesKey("LEAN_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val NUMBER: TextAttributesKey = createTextAttributesKey("LEAN_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING: TextAttributesKey = createTextAttributesKey("LEAN_STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val CHAR: TextAttributesKey = createTextAttributesKey("LEAN_CHAR", DefaultLanguageHighlighterColors.STRING)
    @JvmField val LINE_COMMENT: TextAttributesKey = createTextAttributesKey("LEAN_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val BLOCK_COMMENT: TextAttributesKey = createTextAttributesKey("LEAN_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    @JvmField val DOC_COMMENT: TextAttributesKey = createTextAttributesKey("LEAN_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    @JvmField val PARENTHESES: TextAttributesKey = createTextAttributesKey("LEAN_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    @JvmField val BRACKETS: TextAttributesKey = createTextAttributesKey("LEAN_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val BRACES: TextAttributesKey = createTextAttributesKey("LEAN_BRACES", DefaultLanguageHighlighterColors.BRACES)
    @JvmField val ANGLE_BRACKETS: TextAttributesKey = createTextAttributesKey("LEAN_ANGLE_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val OPERATOR: TextAttributesKey = createTextAttributesKey("LEAN_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val BAD_CHARACTER: TextAttributesKey = createTextAttributesKey("LEAN_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    /** Semantic-token colors reported by the language server. */
    @JvmField val SEMANTIC_VARIABLE: TextAttributesKey = createTextAttributesKey("LEAN_SEMANTIC_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
    @JvmField val SEMANTIC_PROPERTY: TextAttributesKey = createTextAttributesKey("LEAN_SEMANTIC_PROPERTY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val SEMANTIC_FUNCTION: TextAttributesKey = createTextAttributesKey("LEAN_SEMANTIC_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_CALL)
}
