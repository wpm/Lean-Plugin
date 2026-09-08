package com.github.wpm.lean.highlighting

import com.github.wpm.lean.LeanBundle
import com.github.wpm.lean.LeanIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class LeanColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = LeanIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = LeanSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = LeanBundle.message("colors.display.name")

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor(LeanBundle.message("colors.keyword"), LeanHighlighterColors.KEYWORD),
            AttributesDescriptor(LeanBundle.message("colors.modifier"), LeanHighlighterColors.MODIFIER),
            AttributesDescriptor(LeanBundle.message("colors.command"), LeanHighlighterColors.COMMAND),
            AttributesDescriptor(LeanBundle.message("colors.sort"), LeanHighlighterColors.SORT),
            AttributesDescriptor(LeanBundle.message("colors.sorry"), LeanHighlighterColors.SORRY),
            AttributesDescriptor(LeanBundle.message("colors.boolean"), LeanHighlighterColors.BOOLEAN),
            AttributesDescriptor(LeanBundle.message("colors.identifier"), LeanHighlighterColors.IDENTIFIER),
            AttributesDescriptor(LeanBundle.message("colors.number"), LeanHighlighterColors.NUMBER),
            AttributesDescriptor(LeanBundle.message("colors.string"), LeanHighlighterColors.STRING),
            AttributesDescriptor(LeanBundle.message("colors.char"), LeanHighlighterColors.CHAR),
            AttributesDescriptor(LeanBundle.message("colors.line.comment"), LeanHighlighterColors.LINE_COMMENT),
            AttributesDescriptor(LeanBundle.message("colors.block.comment"), LeanHighlighterColors.BLOCK_COMMENT),
            AttributesDescriptor(LeanBundle.message("colors.doc.comment"), LeanHighlighterColors.DOC_COMMENT),
            AttributesDescriptor(LeanBundle.message("colors.parentheses"), LeanHighlighterColors.PARENTHESES),
            AttributesDescriptor(LeanBundle.message("colors.brackets"), LeanHighlighterColors.BRACKETS),
            AttributesDescriptor(LeanBundle.message("colors.braces"), LeanHighlighterColors.BRACES),
            AttributesDescriptor(LeanBundle.message("colors.angle.brackets"), LeanHighlighterColors.ANGLE_BRACKETS),
            AttributesDescriptor(LeanBundle.message("colors.operator"), LeanHighlighterColors.OPERATOR),
            AttributesDescriptor(LeanBundle.message("colors.bad.character"), LeanHighlighterColors.BAD_CHARACTER),
            AttributesDescriptor(LeanBundle.message("colors.semantic.variable"), LeanHighlighterColors.SEMANTIC_VARIABLE),
            AttributesDescriptor(LeanBundle.message("colors.semantic.property"), LeanHighlighterColors.SEMANTIC_PROPERTY),
            AttributesDescriptor(LeanBundle.message("colors.semantic.function"), LeanHighlighterColors.SEMANTIC_FUNCTION),
        )

        private val DEMO_TEXT = """
            /-! Module documentation for `Demo`. -/
            import Mathlib.Tactic

            namespace Demo

            /-- The natural numbers are closed under addition. -/
            theorem add_comm' (a b : ℕ) : a + b = b + a := by
              induction a with
              | zero => simp
              | succ n ih => omega

            structure Point where
              x : Float
              y : Float
              deriving Repr

            private def origin : Point := ⟨0.0, 0.0⟩

            #eval s!"origin = {repr origin}"  -- prints the point

            example : ∀ (p : Prop), p → p := fun _ h => h

            /- A nested /- block -/ comment -/
            theorem todo : 1 + 1 = 2 := sorry

            end Demo
        """.trimIndent()
    }
}
