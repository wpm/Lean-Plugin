package com.github.wpm.lean.lsp

import com.github.wpm.lean.infoview.LeanInfoviewService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.eclipse.lsp4j.Range
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/** Remembers the latest `$/lean/fileProgress` notification per file. */
@Service(Service.Level.PROJECT)
class LeanFileProgressTracker(private val project: Project) {
    private val processingByFile = ConcurrentHashMap<String, List<Range>>()

    fun update(params: LeanFileProgressParams) {
        val uri = params.textDocument?.uri ?: return
        val ranges = params.processing.orEmpty().mapNotNull { it.range }
        if (ranges.isEmpty()) processingByFile.remove(normalize(uri)) else processingByFile[normalize(uri)] = ranges
        LeanInfoviewService.getInstance(project).requestRefresh()
    }

    /** Is the server still elaborating the given (zero-based) line? */
    fun isProcessing(fileUri: String, line: Int): Boolean =
        processingByFile[normalize(fileUri)]?.any { line >= it.start.line && line <= it.end.line } ?: false

    companion object {
        fun getInstance(project: Project): LeanFileProgressTracker = project.service()

        /** Lean and the IDE may percent-encode URIs differently; compare decoded paths. */
        private fun normalize(uri: String): String = runCatching { URI(uri).path }.getOrNull() ?: uri
    }
}
