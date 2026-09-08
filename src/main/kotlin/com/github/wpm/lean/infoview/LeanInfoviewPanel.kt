package com.github.wpm.lean.infoview

import com.github.wpm.lean.LeanBundle
import com.github.wpm.lean.LeanFileType
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JPanel

/** The Swing side of the infoview: a header line and a read-only Lean-highlighted editor. */
class LeanInfoviewPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {
    private val headerLabel = JBLabel(LeanBundle.message("infoview.title")).apply {
        border = JBUI.Borders.empty(4, 8)
    }
    private val statusLabel = JBLabel().apply {
        border = JBUI.Borders.empty(4, 8)
        foreground = UIUtil.getContextHelpForeground()
    }
    private val editorField = EditorTextField(EditorFactory.getInstance().createDocument(""), project, LeanFileType, true, false).apply {
        setFontInheritedFromLAF(false)
        addSettingsProvider { editor ->
            editor.settings.isLineNumbersShown = false
            editor.settings.isFoldingOutlineShown = false
            editor.settings.isUseSoftWraps = true
            editor.settings.isCaretRowShown = false
            editor.settings.additionalLinesCount = 1
            editor.setVerticalScrollbarVisible(true)
            editor.setHorizontalScrollbarVisible(true)
        }
    }

    init {
        val actions = DefaultActionGroup().apply {
            add(RefreshAction())
            ActionManager.getInstance().getAction("Lean.RestartServer")?.let { add(it) }
        }
        val toolbar = ActionManager.getInstance().createActionToolbar("LeanInfoview", actions, true)
        toolbar.targetComponent = this

        val labels = JPanel(BorderLayout()).apply {
            add(headerLabel, BorderLayout.WEST)
            add(statusLabel, BorderLayout.EAST)
        }
        val north = JPanel(BorderLayout()).apply {
            add(labels, BorderLayout.CENTER)
            add(toolbar.component, BorderLayout.EAST)
        }
        add(north, BorderLayout.NORTH)
        add(editorField, BorderLayout.CENTER)
    }

    /** Must be called on the EDT. */
    fun render(model: InfoviewModel) {
        headerLabel.text = model.header.ifEmpty { LeanBundle.message("infoview.title") }
        statusLabel.text = model.statusLine()
        editorField.text = model.renderBody()
    }

    override fun dispose() {
        LeanInfoviewService.getInstance(project).detach(this)
    }

    private inner class RefreshAction : DumbAwareAction(
        LeanBundle.message("action.Lean.RefreshInfoview.text"),
        LeanBundle.message("action.Lean.RefreshInfoview.description"),
        AllIcons.Actions.Refresh,
    ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            LeanInfoviewService.getInstance(project).requestRefresh()
        }
    }
}
