package com.github.wpm.lean.lsp

import com.github.wpm.lean.LeanFileType
import com.github.wpm.lean.LeanIcons
import com.github.wpm.lean.settings.LeanConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem

/** Starts a Lean language server for the package that contains each opened `.lean` file. */
@Suppress("DEPRECATION")
class LeanLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
        if (file.fileType != LeanFileType) return
        val root = LeanProjectRoots.findRoot(project, file) ?: return
        serverStarter.ensureServerStarted(LeanLspServerDescriptor(project, root))
    }

    override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
        LspServerWidgetItem(lspServer, currentFile, LeanIcons.FILE, LeanConfigurable::class.java)
}
