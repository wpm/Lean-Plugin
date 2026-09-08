package com.github.wpm.lean.parser

import com.github.wpm.lean.LeanLanguage
import com.github.wpm.lean.lexer.LeanLexer
import com.github.wpm.lean.lexer.LeanTokenTypes
import com.github.wpm.lean.psi.LeanFile
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * A flat "parser": the file node directly contains the lexer tokens.
 *
 * Structural understanding of Lean code (declarations, references, types) comes from the
 * language server, so the PSI only needs to exist for editor features that are keyed on a
 * language: syntax highlighting, brace matching, commenting and the LSP integration itself.
 */
class LeanParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = LeanLexer()

    override fun createParser(project: Project?): PsiParser = PsiParser { root, builder ->
        val marker = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        marker.done(root)
        builder.treeBuilt
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = LeanTokenTypes.COMMENTS

    override fun getStringLiteralElements(): TokenSet = LeanTokenTypes.STRINGS

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LeanFile(viewProvider)

    companion object {
        @JvmField
        val FILE: IFileElementType = IFileElementType(LeanLanguage)
    }
}
