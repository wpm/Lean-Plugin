package com.github.wpm.lean.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/** Per-project settings, stored in `.idea/lean.xml`. */
@Service(Service.Level.PROJECT)
@State(name = "LeanSettings", storages = [Storage("lean.xml")])
class LeanSettings : SimplePersistentStateComponent<LeanSettings.State>(State()) {
    class State : BaseState() {
        /** Directory containing `lean` and `lake`; empty means PATH, then `~/.elan/bin`. */
        var toolchainDir: String? by string()

        /** Extra command-line arguments appended to the language server invocation. */
        var serverArgs: String? by string()

        /** Replace `\alpha` as soon as it is unambiguous rather than on the next non-matching character. */
        var eagerReplacement: Boolean by property(true)
    }

    companion object {
        fun getInstance(project: Project): LeanSettings = project.service()
    }
}
