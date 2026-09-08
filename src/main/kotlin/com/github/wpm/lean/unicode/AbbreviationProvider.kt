package com.github.wpm.lean.unicode

/**
 * Answers queries against the table of Unicode abbreviations (`\alpha` → `α`).
 *
 * The lookup semantics mirror `AbbreviationProvider` from the official VS Code
 * extension's `lean4-unicode-input` package so that typing feels the same in both editors.
 * This class has no IntelliJ Platform dependencies.
 */
class AbbreviationProvider(symbolsByAbbreviation: Map<String, String>) {
    private val symbols: Map<String, String> = LinkedHashMap(symbolsByAbbreviation)

    /** Abbreviations sorted by length (stable), so the shortest match wins ties. */
    private val entriesByLength: List<Map.Entry<String, String>> = symbols.entries.sortedBy { it.key.length }

    private val replacementCache = HashMap<String, String?>()

    val size: Int get() = symbols.size

    fun getSymbolForAbbreviation(abbreviation: String): String? = symbols[abbreviation]

    /** All symbols whose abbreviation starts with [prefix], shortest abbreviation first. */
    fun findSymbolsByAbbreviationPrefix(prefix: String): List<String> =
        entriesByLength.filter { it.key.startsWith(prefix) }.map { it.value }

    fun hasAbbreviationWithPrefix(prefix: String): Boolean =
        entriesByLength.any { it.key.startsWith(prefix) }

    /** Does [abbreviation] name exactly one symbol, with no longer abbreviation extending it? */
    fun isUniqueAndComplete(abbreviation: String): Boolean =
        symbols.containsKey(abbreviation) && findSymbolsByAbbreviationPrefix(abbreviation).size == 1

    /**
     * The replacement text for a typed abbreviation (without the leader character), possibly
     * still containing the `$CURSOR` marker. The longest non-empty prefix that matches an
     * abbreviation is converted, and the remainder is kept verbatim:
     *
     * - `alp` → `α`
     * - `alp7` → `α7`
     * - `` (empty) → `null`
     */
    fun getReplacementText(abbreviation: String): String? {
        if (abbreviation.isEmpty()) return null
        if (replacementCache.containsKey(abbreviation)) return replacementCache[abbreviation]
        val result = computeReplacementText(abbreviation)
        replacementCache[abbreviation] = result
        return result
    }

    private fun computeReplacementText(abbreviation: String): String? {
        findSymbolsByAbbreviationPrefix(abbreviation).firstOrNull()?.let { return it }
        val prefixReplacement = getReplacementText(abbreviation.dropLast(1)) ?: return null
        return prefixReplacement + abbreviation.takeLast(1)
    }

    /** Like [getReplacementText], but with the `$CURSOR` marker resolved into a caret offset. */
    fun getReplacement(abbreviation: String): Replacement? {
        val raw = getReplacementText(abbreviation) ?: return null
        val cursor = raw.indexOf(CURSOR_MARKER)
        return if (cursor < 0) Replacement(raw, null) else Replacement(raw.replace(CURSOR_MARKER, ""), cursor)
    }

    data class Replacement(val text: String, val cursorOffset: Int?)

    companion object {
        const val CURSOR_MARKER: String = "\$CURSOR"
        const val DEFAULT_LEADER: Char = '\\'
    }
}
