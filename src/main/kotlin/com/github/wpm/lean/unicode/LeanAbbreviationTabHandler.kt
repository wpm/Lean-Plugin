package com.github.wpm.lean.unicode

import com.github.wpm.lean.LeanBundle
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

/** Tab completes an in-progress abbreviation (`\al<Tab>` gives `α`) instead of inserting a tab. */
class LeanAbbreviationTabHandler(private val original: EditorActionHandler) : EditorActionHandler() {
    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        val tracked = AbbreviationTracker.get(editor)
        val project = editor.project
        if (tracked != null && project != null && editor.caretModel.offset == tracked.endOffset) {
            val text = AbbreviationTracker.currentText(editor, tracked)
            val replacement = text?.takeIf { it.isNotEmpty() }?.let { AbbreviationService.getInstance().provider.getReplacement(it) }
            AbbreviationTracker.clear(editor)
            if (replacement != null) {
                WriteCommandAction.runWriteCommandAction(
                    project,
                    LeanBundle.message("command.replace.abbreviation"),
                    null,
                    Runnable { AbbreviationTracker.applyReplacement(editor, tracked, replacement, moveCaret = true) },
                )
                return
            }
        }
        original.execute(editor, caret, dataContext)
    }

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
        AbbreviationTracker.get(editor) != null || original.isEnabled(editor, caret, dataContext)
}
