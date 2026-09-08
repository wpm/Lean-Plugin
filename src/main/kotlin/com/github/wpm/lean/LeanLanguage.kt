package com.github.wpm.lean

import com.intellij.lang.Language

object LeanLanguage : Language("Lean4") {
    private fun readResolve(): Any = LeanLanguage

    override fun getDisplayName(): String = "Lean 4"
}
