package com.github.wpm.lean.unicode

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbbreviationProviderTest {
    private val table: LinkedHashMap<String, String> = run {
        val stream = checkNotNull(AbbreviationProviderTest::class.java.getResourceAsStream("/lean/abbreviations.json"))
        val type = object : TypeToken<LinkedHashMap<String, String>>() {}.type
        stream.reader(Charsets.UTF_8).use { Gson().fromJson(it, type) }
    }
    private val provider = AbbreviationProvider(table)

    @Test
    fun bundledTableLoads() {
        assertTrue("expected a large table, got ${provider.size}", provider.size > 1000)
        assertEquals("α", provider.getSymbolForAbbreviation("alpha"))
        assertEquals("→", provider.getSymbolForAbbreviation("to"))
        assertEquals("\\", provider.getSymbolForAbbreviation("\\"))
    }

    @Test
    fun exactAbbreviationIsReplaced() {
        assertEquals("α", provider.getReplacementText("alpha"))
        assertEquals("→", provider.getReplacementText("to"))
    }

    @Test
    fun prefixIsReplacedAndRemainderKept() {
        // Mirrors the documented VS Code behaviour: "alp7" -> "α7".
        assertEquals("α", provider.getReplacementText("alp"))
        assertEquals("α7", provider.getReplacementText("alp7"))
    }

    @Test
    fun emptyAndUnknownAbbreviations() {
        assertNull(provider.getReplacementText(""))
        assertNull(provider.getReplacementText(" "))
        assertFalse(provider.hasAbbreviationWithPrefix("zzzz"))
    }

    @Test
    fun shortestAbbreviationWinsAmongPrefixMatches() {
        assertEquals("α", provider.findSymbolsByAbbreviationPrefix("a").first())
        assertTrue(provider.findSymbolsByAbbreviationPrefix("a").size > 1)
    }

    @Test
    fun uniqueAndComplete() {
        // "to" is complete but not unique: "top", "toc", ... extend it.
        assertFalse(provider.isUniqueAndComplete("to"))
        assertFalse(provider.isUniqueAndComplete("alph"))
        val unique = table.keys.first { key -> table.keys.none { it != key && it.startsWith(key) } }
        assertTrue(unique, provider.isUniqueAndComplete(unique))
    }

    @Test
    fun cursorMarkerBecomesCaretOffset() {
        val replacement = checkNotNull(provider.getReplacement("<>"))
        assertEquals("⟨⟩", replacement.text)
        assertEquals(1, replacement.cursorOffset)
        assertNull(checkNotNull(provider.getReplacement("alpha")).cursorOffset)
    }
}
