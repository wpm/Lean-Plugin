package com.github.wpm.lean.settings

import com.github.wpm.lean.LeanBundle
import com.github.wpm.lean.lsp.LeanLspServerSupportProvider
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

/** Settings | Languages & Frameworks | Lean 4 */
class LeanConfigurable(private val project: Project) : BoundConfigurable(LeanBundle.message("settings.title")) {
    private val state: LeanSettings.State
        get() = LeanSettings.getInstance(project).state

    override fun createPanel(): DialogPanel = panel {
        group(LeanBundle.message("settings.group.server")) {
            row(LeanBundle.message("settings.toolchain.dir")) {
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle(LeanBundle.message("settings.toolchain.dir.chooser"))
                textFieldWithBrowseButton(descriptor, project)
                    .bindText({ state.toolchainDir.orEmpty() }, { state.toolchainDir = it.trim().ifEmpty { null } })
                    .comment(LeanBundle.message("settings.toolchain.dir.comment"))
                    .align(AlignX.FILL)
            }
            row(LeanBundle.message("settings.server.args")) {
                textField()
                    .bindText({ state.serverArgs.orEmpty() }, { state.serverArgs = it.trim().ifEmpty { null } })
                    .comment(LeanBundle.message("settings.server.args.comment"))
                    .align(AlignX.FILL)
            }
        }
        group(LeanBundle.message("settings.group.editing")) {
            row {
                checkBox(LeanBundle.message("settings.eager.replacement"))
                    .bindSelected({ state.eagerReplacement }, { state.eagerReplacement = it })
                    .comment(LeanBundle.message("settings.eager.replacement.comment"))
            }
        }
    }

    override fun apply() {
        val before = state.toolchainDir to state.serverArgs
        super.apply()
        if (before != (state.toolchainDir to state.serverArgs)) {
            @Suppress("DEPRECATION")
            LspServerManager.getInstance(project).stopAndRestartIfNeeded(LeanLspServerSupportProvider::class.java)
        }
    }
}
