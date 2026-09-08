package com.github.wpm.lean

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object LeanFileType : LanguageFileType(LeanLanguage) {
    override fun getName(): String = "Lean"

    override fun getDescription(): String = LeanBundle.message("filetype.lean.description")

    override fun getDefaultExtension(): String = "lean"

    override fun getIcon(): Icon = LeanIcons.FILE
}
