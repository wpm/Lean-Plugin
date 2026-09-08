package com.github.wpm.lean

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object LeanIcons {
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/lean.svg", LeanIcons::class.java)

    @JvmField
    val TOOL_WINDOW: Icon = IconLoader.getIcon("/icons/toolWindowLean.svg", LeanIcons::class.java)
}
