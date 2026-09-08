package com.github.wpm.lean.lsp

import com.intellij.platform.lsp.api.Lsp4jServer
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentPositionParams
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import java.util.concurrent.CompletableFuture

/**
 * Lean-specific requests on top of the standard LSP server interface.
 *
 * `lsp4j` generates the implementation reflectively from the annotations; the platform
 * uses this interface because [LeanLspServerDescriptor.lsp4jServerClass] points at it.
 * See `src/Lean/Data/Lsp/Extra.lean` in the Lean repository for the wire format.
 */
interface LeanLsp4jServer : Lsp4jServer {
    /** Goals of the tactic proof at the position, or `null` when the position is not inside a tactic block. */
    @JsonRequest("\$/lean/plainGoal")
    fun plainGoal(params: TextDocumentPositionParams): CompletableFuture<PlainGoal?>

    /** Expected type of the term at the position, or `null` when there is none. */
    @JsonRequest("\$/lean/plainTermGoal")
    fun plainTermGoal(params: TextDocumentPositionParams): CompletableFuture<PlainTermGoal?>
}

/** Reply of `$/lean/plainGoal`. */
class PlainGoal {
    /** The goals as Markdown, or something like "no goals" when the proof is complete. */
    var rendered: String? = null

    /** The pretty-printed goals; empty once every goal is accomplished. */
    var goals: List<String>? = null
}

/** Reply of `$/lean/plainTermGoal`. */
class PlainTermGoal {
    var goal: String? = null
    var range: Range? = null
}

/** `initializationOptions` understood by the Lean server (see `Lean.Server.FileWorker`). */
@Suppress("unused")
class LeanInitializationOptions {
    /** We render goals as plain text, so ask for non-interactive diagnostics. */
    val hasWidgets: Boolean = false
}
