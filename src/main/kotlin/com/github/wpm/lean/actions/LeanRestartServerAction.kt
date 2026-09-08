package com.github.wpm.lean.actions

import com.github.wpm.lean.infoview.LeanInfoviewService
import com.github.wpm.lean.lsp.LeanLspServerSupportProvider
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.platform.lsp.api.LspServerManager

/** Tools | Restart Lean Server, also on the infoview toolbar. */
class LeanRestartServerAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        @Suppress("DEPRECATION")
        LspServerManager.getInstance(project).stopAndRestartIfNeeded(LeanLspServerSupportProvider::class.java)
        LeanInfoviewService.getInstance(project).requestRefresh()
    }
}
