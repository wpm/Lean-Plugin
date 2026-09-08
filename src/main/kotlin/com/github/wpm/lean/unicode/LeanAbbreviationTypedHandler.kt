package com.github.wpm.lean.unicode

import com.github.wpm.lean.LeanFileType
import com.github.wpm.lean.settings.LeanSettings
import com.github.wpm.lean.unicode.AbbreviationTracker.LEADER
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Expands Lean Unicode abbreviations while typing: `\alpha` becomes `α`, `\to` becomes `→`.
 *
 * Replacement happens when the typed text can no longer be extended to any abbreviation
 * (the extra character is kept), or eagerly as soon as the abbreviation is unambiguous
 * when that option is enabled. Tab also forces a replacement (see [LeanAbbreviationTabHandler]).
 */
class LeanAbbreviationTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.fileType != LeanFileType) return Result.CONTINUE

        val caretOffset = editor.caretModel.offset // just past the character that was typed
        val tracked = AbbreviationTracker.get(editor)
        if (tracked == null) {
            if (c == LEADER) AbbreviationTracker.start(editor, caretOffset - 1)
            return Result.CONTINUE
        }

        // The typed character must extend the tracked abbreviation contiguously.
        if (caretOffset - 1 != tracked.endOffset) {
            restartIfLeader(editor, c, caretOffset)
            return Result.CONTINUE
        }
        tracked.endOffset = caretOffset
        val text = AbbreviationTracker.currentText(editor, tracked)
        if (text == null) {
            restartIfLeader(editor, c, caretOffset)
            return Result.CONTINUE
        }

        val provider = AbbreviationService.getInstance().provider
        if (provider.hasAbbreviationWithPrefix(text)) {
            val eager = LeanSettings.getInstance(project).state.eagerReplacement
            if (eager && provider.isUniqueAndComplete(text)) {
                val replacement = provider.getReplacement(text)
                AbbreviationTracker.clear(editor)
                if (replacement != null) {
                    AbbreviationTracker.applyReplacement(editor, tracked, replacement, moveCaret = true)
                }
            }
            return Result.CONTINUE
        }

        // No abbreviation extends the typed text: convert the longest matching prefix, keep the rest.
        val replacement = provider.getReplacement(text)
        AbbreviationTracker.clear(editor)
        if (replacement == null) {
            if (c == LEADER) AbbreviationTracker.start(editor, caretOffset - 1)
            return Result.CONTINUE
        }
        AbbreviationTracker.applyReplacement(editor, tracked, replacement, moveCaret = true)
        if (c == LEADER && replacement.cursorOffset == null && replacement.text.endsWith(LEADER)) {
            // `\al\` -> `α\`: the trailing leader starts the next abbreviation.
            AbbreviationTracker.start(editor, tracked.leaderOffset + replacement.text.length - 1)
        }
        return Result.CONTINUE
    }

    private fun restartIfLeader(editor: Editor, c: Char, caretOffset: Int) {
        AbbreviationTracker.clear(editor)
        if (c == LEADER) AbbreviationTracker.start(editor, caretOffset - 1)
    }
}
