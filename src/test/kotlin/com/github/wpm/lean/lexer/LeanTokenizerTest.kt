package com.github.wpm.lean.lexer

import com.github.wpm.lean.lexer.LeanTokenKind.BLOCK_COMMENT
import com.github.wpm.lean.lexer.LeanTokenKind.BOOLEAN
import com.github.wpm.lean.lexer.LeanTokenKind.CHAR
import com.github.wpm.lean.lexer.LeanTokenKind.COMMAND
import com.github.wpm.lean.lexer.LeanTokenKind.DOC_COMMENT
import com.github.wpm.lean.lexer.LeanTokenKind.IDENTIFIER
import com.github.wpm.lean.lexer.LeanTokenKind.KEYWORD
import com.github.wpm.lean.lexer.LeanTokenKind.LANGLE
import com.github.wpm.lean.lexer.LeanTokenKind.LDBRACE
import com.github.wpm.lean.lexer.LeanTokenKind.LINE_COMMENT
import com.github.wpm.lean.lexer.LeanTokenKind.LPAREN
import com.github.wpm.lean.lexer.LeanTokenKind.MODIFIER
import com.github.wpm.lean.lexer.LeanTokenKind.MODULE_DOC_COMMENT
import com.github.wpm.lean.lexer.LeanTokenKind.NUMBER
import com.github.wpm.lean.lexer.LeanTokenKind.RANGLE
import com.github.wpm.lean.lexer.LeanTokenKind.RDBRACE
import com.github.wpm.lean.lexer.LeanTokenKind.RPAREN
import com.github.wpm.lean.lexer.LeanTokenKind.SORRY
import com.github.wpm.lean.lexer.LeanTokenKind.SORT
import com.github.wpm.lean.lexer.LeanTokenKind.STRING
import com.github.wpm.lean.lexer.LeanTokenKind.SYMBOL
import com.github.wpm.lean.lexer.LeanTokenKind.WHITESPACE
import org.junit.Assert.assertEquals
import org.junit.Test

class LeanTokenizerTest {
    private fun kinds(text: String): List<LeanTokenKind> =
        LeanTokenizer.tokenize(text).filter { it.kind != WHITESPACE }.map { it.kind }

    private fun texts(text: String): List<String> =
        LeanTokenizer.tokenize(text).filter { it.kind != WHITESPACE }.map { text.substring(it.start, it.end) }

    @Test
    fun tokensTileTheInputWithoutGaps() {
        val text = "theorem foo (n : ℕ) : n + 0 = n := by\n  simp -- done\n/- c -/ \"s\" 'c' 42"
        val tokens = LeanTokenizer.tokenize(text)
        assertEquals(0, tokens.first().start)
        assertEquals(text.length, tokens.last().end)
        for (i in 1 until tokens.size) {
            assertEquals("gap before token $i", tokens[i - 1].end, tokens[i].start)
        }
    }

    @Test
    fun keywordsModifiersSortsAndSorry() {
        assertEquals(
            listOf(MODIFIER, KEYWORD, IDENTIFIER, SYMBOL, SORT, SYMBOL, SORRY),
            kinds("private theorem foo : Prop := sorry"),
        )
        assertEquals(listOf(BOOLEAN, BOOLEAN), kinds("true false"))
    }

    @Test
    fun dottedNameContainingKeywordIsAnIdentifier() {
        assertEquals(listOf("Foo.theorem", "Nat.succ", "x.y.z"), texts("Foo.theorem Nat.succ x.y.z"))
        assertEquals(listOf(IDENTIFIER, IDENTIFIER, IDENTIFIER), kinds("Foo.theorem Nat.succ x.y.z"))
    }

    @Test
    fun projectionByIndexIsNotPartOfTheIdentifier() {
        assertEquals(listOf("p", ".", "1"), texts("p.1"))
    }

    @Test
    fun unicodeIdentifiersAndSymbols() {
        assertEquals(listOf(IDENTIFIER, IDENTIFIER, IDENTIFIER), kinds("α₁ ℕ x'"))
        assertEquals(listOf(SYMBOL, IDENTIFIER, SYMBOL, IDENTIFIER), kinds("λ x => x"))
        assertEquals(listOf(SYMBOL, IDENTIFIER, SYMBOL, SYMBOL), kinds("∀ x, ·"))
    }

    @Test
    fun commentsIncludingNesting() {
        assertEquals(listOf(LINE_COMMENT, IDENTIFIER), kinds("-- hello\nx"))
        assertEquals(listOf("/- a /- b -/ c -/", "x"), texts("/- a /- b -/ c -/ x"))
        assertEquals(listOf(BLOCK_COMMENT, IDENTIFIER), kinds("/- a /- b -/ c -/ x"))
        assertEquals(listOf(DOC_COMMENT), kinds("/-- doc -/"))
        assertEquals(listOf(MODULE_DOC_COMMENT), kinds("/-! module -/"))
        assertEquals(listOf(BLOCK_COMMENT), kinds("/- unterminated"))
    }

    @Test
    fun stringsAndChars() {
        assertEquals(listOf(STRING, IDENTIFIER), kinds("\"a\\\"b\" c"))
        assertEquals(listOf(IDENTIFIER, STRING), kinds("s!\"x = {x}\""))
        assertEquals(listOf(CHAR, CHAR, CHAR, CHAR), kinds("'a' '\\n' 'α' '\\x41'"))
        assertEquals(listOf(IDENTIFIER, SYMBOL, IDENTIFIER), kinds("x' := y''"))
    }

    @Test
    fun numbers() {
        assertEquals(listOf("42", "0x1F", "3.14", "1e10", "0b101", "1_000"), texts("42 0x1F 3.14 1e10 0b101 1_000"))
        assertEquals(listOf(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), kinds("42 0x1F 3.14 1e10 0b101 1_000"))
    }

    @Test
    fun commands() {
        assertEquals(listOf(COMMAND, NUMBER, SYMBOL, NUMBER), kinds("#eval 1 + 1"))
        assertEquals(listOf("#check", "Nat"), texts("#check Nat"))
    }

    @Test
    fun bracketsAreDistinctTokens() {
        assertEquals(listOf(LANGLE, IDENTIFIER, SYMBOL, IDENTIFIER, RANGLE, LDBRACE, IDENTIFIER, RDBRACE, LPAREN, RPAREN), kinds("⟨a, b⟩ ⦃x⦄ ()"))
    }

    @Test
    fun operatorsGroupButStopBeforeComments() {
        assertEquals(listOf(":=", "x", "->", "y", "-- c"), texts(":= x -> y -- c"))
        assertEquals(listOf("a", "<|>", "b"), texts("a <|> b"))
    }

    @Test
    fun guillemetIdentifiers() {
        assertEquals(listOf("«weird name»", "Foo.«bar baz»"), texts("«weird name» Foo.«bar baz»"))
        assertEquals(listOf(IDENTIFIER, IDENTIFIER), kinds("«weird name» Foo.«bar baz»"))
    }
}
