package com.github.wpm.lean.psi

import com.github.wpm.lean.LeanFileType
import com.github.wpm.lean.LeanLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class LeanFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LeanLanguage) {
    override fun getFileType(): FileType = LeanFileType

    override fun toString(): String = "Lean file"
}
