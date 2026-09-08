package com.github.wpm.lean.infoview

import com.github.wpm.lean.LeanBundle
import com.github.wpm.lean.LeanFileType
import com.github.wpm.lean.lsp.LeanFileProgressTracker
import com.github.wpm.lean.lsp.LeanLsp4jServer
import com.github.wpm.lean.lsp.LeanLspServerSupportProvider
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentPositionParams
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Keeps the infoview in sync with the caret: debounces editor events, asks the Lean server
 * for the goal state at the caret, gathers the diagnostics on the caret line and pushes the
 * result to every attached [LeanInfoviewPanel].
 */
@Service(Service.Level.PROJECT)
class LeanInfoviewService(private val project: Project, private val scope: CoroutineScope) : Disposable {
    private val panels = CopyOnWriteArrayList<LeanInfoviewPanel>()
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @Volatile
    private var lastModel: InfoviewModel = InfoviewModel.idle(LeanBundle.message("infoview.status.no.file"))

    init {
        val multicaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                if (event.editor.project == project && isLeanDocument(event.editor.document)) requestRefresh()
            }
        }, this)
        multicaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                if (isLeanDocument(event.document)) requestRefresh()
            }
        }, this)

        val connection = project.messageBus.connect(this)
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) = requestRefresh()
        })
        // Diagnostics arrive through the daemon; refresh the "Messages" section once it settles.
        connection.subscribe(DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC, object : DaemonCodeAnalyzer.DaemonListener {
            override fun daemonFinished() = requestRefresh()
        })

        scope.launch {
            refreshRequests.collectLatest {
                delay(DEBOUNCE_MS)
                refresh()
            }
        }
    }

    fun attach(panel: LeanInfoviewPanel) {
        panels += panel
        panel.render(lastModel)
        requestRefresh()
    }

    fun detach(panel: LeanInfoviewPanel) {
        panels -= panel
    }

    fun requestRefresh() {
        if (panels.isEmpty()) return
        refreshRequests.tryEmit(Unit)
    }

    override fun dispose() {
        panels.clear()
    }

    private suspend fun refresh() {
        if (project.isDisposed) return
        val location = withContext(Dispatchers.EDT) { captureLocation() }
        val model = try {
            buildModel(location)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            LOG.warn("Failed to refresh the Lean infoview", e)
            InfoviewModel.idle(LeanBundle.message("infoview.status.error", e.message ?: e.javaClass.simpleName))
        }
        lastModel = model
        withContext(Dispatchers.EDT) {
            for (panel in panels) panel.render(model)
        }
    }

    private class Location(val file: VirtualFile, val document: Document, val line: Int, val column: Int)

    private fun captureLocation(): Location? {
        if (project.isDisposed) return null
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
        val document = editor.document
        val file = FileDocumentManager.getInstance().getFile(document) ?: return null
        if (file.fileType != LeanFileType) return null
        val offset = editor.caretModel.offset
        val line = document.getLineNumber(offset)
        val column = offset - document.getLineStartOffset(line)
        return Location(file, document, line, column)
    }

    private suspend fun buildModel(location: Location?): InfoviewModel {
        if (location == null) return InfoviewModel.idle(LeanBundle.message("infoview.status.no.file"))
        val header = "${location.file.name}:${location.line + 1}:${location.column + 1}"

        val server = findServer(location.file)
            ?: return InfoviewModel.idle(LeanBundle.message("infoview.status.server.not.running"), header)
        if (server.state != LspServerState.Running) {
            return InfoviewModel.idle(LeanBundle.message("infoview.status.server.starting"), header)
        }

        val params = TextDocumentPositionParams(server.getDocumentIdentifier(location.file), Position(location.line, location.column))
        val goal = server.sendRequest { (it as LeanLsp4jServer).plainGoal(params) }
        val termGoal = server.sendRequest { (it as LeanLsp4jServer).plainTermGoal(params) }
        val messages = readAction { collectMessages(location) }
        val processing = LeanFileProgressTracker.getInstance(project)
            .isProcessing(server.descriptor.getFileUri(location.file), location.line)

        return InfoviewModel(
            header = header,
            status = null,
            goals = goal?.goals.orEmpty(),
            hasTacticState = goal != null,
            termGoal = termGoal?.goal,
            messages = messages,
            processing = processing,
        )
    }

    @Suppress("DEPRECATION")
    private fun findServer(file: VirtualFile): LspServer? =
        LspServerManager.getInstance(project)
            .getServersForProvider(LeanLspServerSupportProvider::class.java)
            .firstOrNull { it.descriptor.isSupportedFile(file) }

    /** Diagnostics (from the LSP server, via the highlighting daemon) that touch the caret line. */
    private fun collectMessages(location: Location): List<InfoviewMessage> {
        val document = location.document
        if (location.line >= document.lineCount) return emptyList()
        val start = document.getLineStartOffset(location.line)
        val end = document.getLineEndOffset(location.line)
        val result = ArrayList<InfoviewMessage>()
        DaemonCodeAnalyzerEx.processHighlights(document, project, null, start, end) { info ->
            val description = info.description
            if (!description.isNullOrBlank()) {
                result += InfoviewMessage(info.severity.name.lowercase(), description)
            }
            true
        }
        return result
    }

    private fun isLeanDocument(document: Document): Boolean =
        FileDocumentManager.getInstance().getFile(document)?.extension == "lean"

    companion object {
        private const val DEBOUNCE_MS = 150L
        private val LOG = logger<LeanInfoviewService>()

        fun getInstance(project: Project): LeanInfoviewService = project.service()
    }
}
