package com.github.wpm.lean.lexer

/** Token classes produced by [LeanTokenizer]. */
enum class LeanTokenKind {
    WHITESPACE,
    LINE_COMMENT,
    BLOCK_COMMENT,
    DOC_COMMENT,
    MODULE_DOC_COMMENT,
    KEYWORD,
    MODIFIER,
    COMMAND,
    SORT,
    SORRY,
    BOOLEAN,
    IDENTIFIER,
    NUMBER,
    STRING,
    CHAR,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    LBRACE,
    RBRACE,
    LANGLE,
    RANGLE,
    LDBRACE,
    RDBRACE,
    SYMBOL,
    BAD,
}

data class LeanToken(val kind: LeanTokenKind, val start: Int, val end: Int)

/**
 * A hand-written, state-free tokenizer for Lean 4 source text.
 *
 * It has no dependency on the IntelliJ Platform so it can be unit tested in isolation.
 * Every call to [next] returns the token starting at the current position; block comments
 * (which nest in Lean) are consumed whole, so every token boundary is a valid restart point.
 */
class LeanTokenizer(
    private val text: CharSequence,
    start: Int = 0,
    private val end: Int = text.length,
) {
    private var pos = start

    fun next(): LeanToken? {
        if (pos >= end) return null
        val start = pos
        val c = text[pos]
        val kind = when {
            c.isWhitespace() -> lexWhitespace()
            c == '-' && peek(1) == '-' -> lexLineComment()
            c == '/' && peek(1) == '-' -> lexBlockComment()
            c == '"' -> lexString()
            c == '\'' && isCharLiteralStart() -> lexChar()
            c.isAsciiDigit() -> lexNumber()
            c == '#' && isIdentFirst(peek(1)) -> lexCommand()
            isIdentFirst(c) || c == '«' -> lexIdentifier()
            else -> lexSymbolOrBracket()
        }
        return LeanToken(kind, start, pos)
    }

    private fun peek(offset: Int): Char = if (pos + offset < end) text[pos + offset] else ' '

    private fun lexWhitespace(): LeanTokenKind {
        while (pos < end && text[pos].isWhitespace()) pos++
        return LeanTokenKind.WHITESPACE
    }

    private fun lexLineComment(): LeanTokenKind {
        while (pos < end && text[pos] != '\n') pos++
        return LeanTokenKind.LINE_COMMENT
    }

    private fun lexBlockComment(): LeanTokenKind {
        val kind = when (peek(2)) {
            // `/--/` is an ordinary (empty) comment, `/-- text -/` is a doc comment.
            '-' -> if (peek(3) == '/') LeanTokenKind.BLOCK_COMMENT else LeanTokenKind.DOC_COMMENT
            '!' -> LeanTokenKind.MODULE_DOC_COMMENT
            else -> LeanTokenKind.BLOCK_COMMENT
        }
        pos += 2
        var depth = 1
        while (pos < end && depth > 0) {
            if (text[pos] == '/' && peek(1) == '-') {
                depth++
                pos += 2
            } else if (text[pos] == '-' && peek(1) == '/') {
                depth--
                pos += 2
            } else {
                pos++
            }
        }
        return kind
    }

    private fun lexString(): LeanTokenKind {
        pos++ // opening quote
        while (pos < end) {
            val c = text[pos]
            if (c == '\\' && pos + 1 < end) {
                pos += 2
                continue
            }
            pos++
            if (c == '"') break
        }
        return LeanTokenKind.STRING
    }

    private fun isCharLiteralStart(): Boolean {
        // `'a'`, `'\n'`, `'\x41'`, `'α'`, `'😀'`
        if (peek(1) == '\\') {
            var i = 2
            while (pos + i < end && text[pos + i] != '\'' && text[pos + i] != '\n' && i < 8) i++
            return peek(i) == '\''
        }
        val codePointLength = if (Character.isHighSurrogate(peek(1)) && Character.isLowSurrogate(peek(2))) 2 else 1
        return peek(1) != '\'' && peek(1) != '\n' && peek(1) != ' ' && peek(1 + codePointLength) == '\''
    }

    private fun lexChar(): LeanTokenKind {
        pos++ // opening quote
        while (pos < end && text[pos] != '\'') pos++
        if (pos < end) pos++ // closing quote
        return LeanTokenKind.CHAR
    }

    private fun lexNumber(): LeanTokenKind {
        if (text[pos] == '0' && (peek(1) == 'x' || peek(1) == 'X')) {
            pos += 2
            while (pos < end && (text[pos].isHexDigit() || text[pos] == '_')) pos++
            return LeanTokenKind.NUMBER
        }
        if (text[pos] == '0' && (peek(1) == 'b' || peek(1) == 'B' || peek(1) == 'o' || peek(1) == 'O')) {
            pos += 2
            while (pos < end && (text[pos].isAsciiDigit() || text[pos] == '_')) pos++
            return LeanTokenKind.NUMBER
        }
        while (pos < end && (text[pos].isAsciiDigit() || text[pos] == '_')) pos++
        if (pos < end && text[pos] == '.' && peek(1).isAsciiDigit()) {
            pos++
            while (pos < end && text[pos].isAsciiDigit()) pos++
        }
        if (pos < end && (text[pos] == 'e' || text[pos] == 'E')) {
            val signLength = if (peek(1) == '+' || peek(1) == '-') 1 else 0
            if (peek(1 + signLength).isAsciiDigit()) {
                pos += 1 + signLength
                while (pos < end && text[pos].isAsciiDigit()) pos++
            }
        }
        return LeanTokenKind.NUMBER
    }

    private fun lexCommand(): LeanTokenKind {
        pos++ // '#'
        while (pos < end && isIdentRest(text[pos])) pos++
        return LeanTokenKind.COMMAND
    }

    private fun lexIdentifier(): LeanTokenKind {
        val start = pos
        lexIdentifierPart()
        while (pos < end && text[pos] == '.' && (isIdentFirst(peek(1)) || peek(1) == '«')) {
            pos++
            lexIdentifierPart()
        }
        val word = text.subSequence(start, pos).toString()
        return when {
            word in LeanKeywords.KEYWORDS -> LeanTokenKind.KEYWORD
            word in LeanKeywords.MODIFIERS -> LeanTokenKind.MODIFIER
            word in LeanKeywords.SORTS -> LeanTokenKind.SORT
            word in LeanKeywords.SORRY_LIKE -> LeanTokenKind.SORRY
            word in LeanKeywords.BOOLEANS -> LeanTokenKind.BOOLEAN
            else -> LeanTokenKind.IDENTIFIER
        }
    }

    private fun lexIdentifierPart() {
        if (pos < end && text[pos] == '«') {
            pos++
            while (pos < end && text[pos] != '»' && text[pos] != '\n') pos++
            if (pos < end && text[pos] == '»') pos++
            return
        }
        while (pos < end && isIdentRest(text[pos])) pos++
    }

    private fun lexSymbolOrBracket(): LeanTokenKind {
        val c = text[pos]
        val bracket = BRACKETS[c]
        if (bracket != null) {
            pos++
            return bracket
        }
        if (c in OPERATOR_CHARS) {
            while (pos < end && text[pos] in OPERATOR_CHARS && !startsComment()) pos++
            return LeanTokenKind.SYMBOL
        }
        if (Character.isHighSurrogate(c) && pos + 1 < end && Character.isLowSurrogate(text[pos + 1])) {
            pos += 2
        } else {
            pos++
        }
        return if (c.isISOControl()) LeanTokenKind.BAD else LeanTokenKind.SYMBOL
    }

    private fun startsComment(): Boolean =
        (text[pos] == '-' && peek(1) == '-') || (text[pos] == '/' && peek(1) == '-')

    companion object {
        private const val OPERATOR_CHARS = ":=<>+-*/\\|&^%$?~!@#"

        private val BRACKETS: Map<Char, LeanTokenKind> = mapOf(
            '(' to LeanTokenKind.LPAREN,
            ')' to LeanTokenKind.RPAREN,
            '[' to LeanTokenKind.LBRACKET,
            ']' to LeanTokenKind.RBRACKET,
            '{' to LeanTokenKind.LBRACE,
            '}' to LeanTokenKind.RBRACE,
            '⟨' to LeanTokenKind.LANGLE,
            '⟩' to LeanTokenKind.RANGLE,
            '⦃' to LeanTokenKind.LDBRACE,
            '⦄' to LeanTokenKind.RDBRACE,
        )

        fun tokenize(text: CharSequence): List<LeanToken> {
            val tokenizer = LeanTokenizer(text)
            val result = ArrayList<LeanToken>()
            while (true) {
                result += tokenizer.next() ?: break
            }
            return result
        }

        private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

        private fun Char.isHexDigit(): Boolean = isAsciiDigit() || this in 'a'..'f' || this in 'A'..'F'

        private fun Char.isAsciiLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

        /** Mirrors `Lean.isLetterLike`. */
        fun isLetterLike(c: Char): Boolean {
            val code = c.code
            return (code in 0x3b1..0x3c9 && code != 0x3bb) ||               // lower Greek, except lambda
                (code in 0x391..0x3a9 && code != 0x3a0 && code != 0x3a3) || // upper Greek, except Pi and Sigma
                (code in 0x3ca..0x3fb) ||                                   // Coptic
                (code in 0x1f00..0x1ffe) ||                                 // polytonic Greek
                (code in 0x2100..0x214f)                                    // letter-like symbols (ℕ, ℤ, ℝ, ...)
        }

        /** Mirrors `Lean.isSubScriptAlnum`. */
        fun isSubscriptAlnum(c: Char): Boolean {
            val code = c.code
            return (code in 0x2080..0x2089) || (code in 0x2090..0x209c) || (code in 0x1d62..0x1d6a)
        }

        fun isIdentFirst(c: Char): Boolean = c.isAsciiLetter() || c == '_' || isLetterLike(c)

        fun isIdentRest(c: Char): Boolean =
            isIdentFirst(c) || c.isAsciiDigit() || c == '\'' || c == '!' || c == '?' || isSubscriptAlnum(c)
    }
}
