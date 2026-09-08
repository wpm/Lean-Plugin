package com.github.wpm.lean.unicode

import com.github.wpm.lean.LeanBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key

/** An abbreviation the user is currently typing: `\` at [leaderOffset], text up to [endOffset]. */
class TrackedAbbreviation(val leaderOffset: Int) {
    /** Offset just past the last typed abbreviation character. */
    @Volatile
    var endOffset: Int = leaderOffset + 1

    val abbreviationStart: Int get() = leaderOffset + 1
}

/**
 * Keeps at most one in-progress abbreviation per editor.
 *
 * The typed-character handler extends it, and the caret listener registered in [start]
 * finalizes it when the caret leaves the abbreviation (mirroring the VS Code behaviour).
 */
object AbbreviationTracker {
    const val LEADER: Char = AbbreviationProvider.DEFAULT_LEADER

    private val KEY = Key.create<TrackedAbbreviation>("com.github.wpm.lean.trackedAbbreviation")
    private val DISPOSABLE_KEY = Key.create<Disposable>("com.github.wpm.lean.trackedAbbreviation.disposable")

    fun get(editor: Editor): TrackedAbbreviation? = editor.getUserData(KEY)

    fun start(editor: Editor, leaderOffset: Int): TrackedAbbreviation {
        clear(editor)
        val tracked = TrackedAbbreviation(leaderOffset)
        editor.putUserData(KEY, tracked)

        val disposable = Disposer.newDisposable("Lean abbreviation tracking")
        EditorUtil.disposeWithEditor(editor, disposable)
        editor.putUserData(DISPOSABLE_KEY, disposable)
        editor.caretModel.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                // Defer: when the caret moves because a character was typed, the typed handler
                // has not yet extended the tracked range at this point.
                ApplicationManager.getApplication().invokeLater { finishIfCaretLeft(editor, tracked) }
            }
        }, disposable)
        return tracked
    }

    fun clear(editor: Editor) {
        editor.putUserData(KEY, null)
        editor.getUserData(DISPOSABLE_KEY)?.let {
            editor.putUserData(DISPOSABLE_KEY, null)
            Disposer.dispose(it)
        }
    }

    /** The abbreviation text typed so far (without the leader), or null if the tracked range is no longer intact. */
    fun currentText(editor: Editor, tracked: TrackedAbbreviation): String? {
        val text = editor.document.charsSequence
        val start = tracked.abbreviationStart
        val end = tracked.endOffset
        if (tracked.leaderOffset < 0 || end > text.length || start > end) return null
        if (text[tracked.leaderOffset] != LEADER) return null
        val abbreviation = text.subSequence(start, end).toString()
        if (abbreviation.any { it.isWhitespace() }) return null
        return abbreviation
    }

    /** Replaces the tracked range with [replacement]. Must be called inside a write action. */
    fun applyReplacement(
        editor: Editor,
        tracked: TrackedAbbreviation,
        replacement: AbbreviationProvider.Replacement,
        moveCaret: Boolean,
    ) {
        editor.document.replaceString(tracked.leaderOffset, tracked.endOffset, replacement.text)
        if (moveCaret) {
            val target = tracked.leaderOffset + (replacement.cursorOffset ?: replacement.text.length)
            editor.caretModel.moveToOffset(target)
            editor.selectionModel.removeSelection()
        }
    }

    private fun finishIfCaretLeft(editor: Editor, tracked: TrackedAbbreviation) {
        if (editor.isDisposed || get(editor) !== tracked) return
        val caret = editor.caretModel.offset
        if (caret in tracked.leaderOffset..tracked.endOffset) return

        val project = editor.project
        val text = currentText(editor, tracked)
        clear(editor)
        if (project == null || text.isNullOrEmpty()) return
        val replacement = AbbreviationService.getInstance().provider.getReplacement(text) ?: return
        WriteCommandAction.runWriteCommandAction(
            project,
            LeanBundle.message("command.replace.abbreviation"),
            null,
            Runnable { applyReplacement(editor, tracked, replacement, moveCaret = false) },
        )
    }
}
