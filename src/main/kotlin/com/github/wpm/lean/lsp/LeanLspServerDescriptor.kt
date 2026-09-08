package com.github.wpm.lean.lsp

import com.github.wpm.lean.LeanFileType
import com.github.wpm.lean.LeanLanguage
import com.github.wpm.lean.highlighting.LeanHighlighterColors
import com.github.wpm.lean.settings.LeanSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.Lsp4jServer
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspSemanticTokensCustomizer
import com.intellij.platform.lsp.api.customization.LspSemanticTokensSupport
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.SemanticTokenTypes

/**
 * Describes one Lean language server, serving the `.lean` files below a single package [root].
 *
 * Built on the pre-2026.1.4 class names (`LspServerDescriptor`), which remain available,
 * deprecated, in newer IDEs, so one binary covers 2025.2 through 2026.x.
 */
@Suppress("DEPRECATION")
class LeanLspServerDescriptor(project: Project, val root: VirtualFile) : LspServerDescriptor(project, "Lean 4", root) {

    override fun isSupportedFile(file: VirtualFile): Boolean =
        file.fileType == LeanFileType && VfsUtilCore.isAncestor(root, file, false)

    @Throws(ExecutionException::class)
    override fun createCommandLine(): GeneralCommandLine {
        try {
            return LeanToolchain.createServerCommandLine(root, LeanSettings.getInstance(project).state)
        } catch (e: ExecutionException) {
            LeanNotifications.serverStartFailed(project, e.message)
            throw e
        }
    }

    override fun createInitializationOptions(): Any = LeanInitializationOptions()

    /** The VS Code extension registers the language as `lean4`; the server accepts `lean` too. */
    override fun getLanguageId(file: VirtualFile): String = "lean4"

    override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient = LeanLsp4jClient(handler, project)

    override val lsp4jServerClass: Class<out Lsp4jServer>
        get() = LeanLsp4jServer::class.java

    override val lspCustomization: LspCustomization
        get() = LeanLspCustomization
}

object LeanLspCustomization : LspCustomization() {
    /** Lean's server declares a custom token type for `sorry`-like terms. */
    private const val LEAN_SORRY_LIKE = "leanSorryLike"

    override val semanticTokensCustomizer: LspSemanticTokensCustomizer = object : LspSemanticTokensSupport() {
        // The platform only asks for semantic tokens in plain-text / TextMate files by default;
        // our PSI is a flat token list, so the server's classification is very much wanted.
        override fun shouldAskServerForSemanticTokens(psiFile: PsiFile): Boolean = psiFile.language == LeanLanguage

        override fun getTextAttributesKey(tokenType: String, modifiers: List<String>): TextAttributesKey? = when (tokenType) {
            SemanticTokenTypes.Keyword -> LeanHighlighterColors.KEYWORD
            SemanticTokenTypes.Variable -> LeanHighlighterColors.SEMANTIC_VARIABLE
            SemanticTokenTypes.Property -> LeanHighlighterColors.SEMANTIC_PROPERTY
            SemanticTokenTypes.Function -> LeanHighlighterColors.SEMANTIC_FUNCTION
            LEAN_SORRY_LIKE -> LeanHighlighterColors.SORRY
            else -> super.getTextAttributesKey(tokenType, modifiers)
        }
    }
}
