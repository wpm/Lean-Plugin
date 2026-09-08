package com.github.wpm.lean.editor

import com.intellij.lang.Commenter

class LeanCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "--"

    override fun getBlockCommentPrefix(): String = "/-"

    override fun getBlockCommentSuffix(): String = "-/"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
