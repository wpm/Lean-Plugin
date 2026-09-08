package com.github.wpm.lean.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification

/** Handles the Lean-specific notifications the server sends to the client. */
class LeanLsp4jClient(
    handler: LspServerNotificationsHandler,
    private val project: Project,
) : Lsp4jClient(handler) {

    /** Which ranges of a file the server is still elaborating. */
    @JsonNotification("\$/lean/fileProgress")
    fun fileProgress(params: LeanFileProgressParams) {
        if (project.isDisposed) return
        LeanFileProgressTracker.getInstance(project).update(params)
    }

    // Sent for the import graph; acknowledged so lsp4j does not log "unsupported notification".
    @JsonNotification("\$/lean/ileanInfoUpdate")
    fun ileanInfoUpdate(@Suppress("UNUSED_PARAMETER") params: Any?) {
    }

    @JsonNotification("\$/lean/ileanInfoFinal")
    fun ileanInfoFinal(@Suppress("UNUSED_PARAMETER") params: Any?) {
    }
}

class LeanFileProgressParams {
    var textDocument: VersionedTextDocumentIdentifier? = null
    var processing: List<LeanFileProgressProcessingInfo>? = null
}

class LeanFileProgressProcessingInfo {
    var range: Range? = null

    /** 1 = processing, 2 = fatal error (see `LeanFileProgressKind`). */
    var kind: Int = 1
}
