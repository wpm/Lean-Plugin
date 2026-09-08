package com.github.wpm.lean.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

/**
 * Locates the Lean package that a file belongs to. One language server is started per package
 * root, in the same way the VS Code extension does it.
 */
object LeanProjectRoots {
    private const val LAKEFILE_LEAN = "lakefile.lean"
    private const val LAKEFILE_TOML = "lakefile.toml"
    private const val LEAN_TOOLCHAIN = "lean-toolchain"

    /**
     * The closest ancestor directory holding a `lakefile.lean`, `lakefile.toml` or `lean-toolchain`,
     * skipping dependency checkouts under `.lake/packages`. Falls back to the project directory,
     * then to the file's own directory, so ad-hoc `.lean` files still get a server.
     */
    fun findRoot(project: Project, file: VirtualFile): VirtualFile? {
        var dir = file.parent
        while (dir != null) {
            if (isPackageRoot(dir)) return dir
            dir = dir.parent
        }
        return project.guessProjectDir() ?: file.parent
    }

    fun hasLakefile(dir: VirtualFile): Boolean =
        dir.findChild(LAKEFILE_LEAN) != null || dir.findChild(LAKEFILE_TOML) != null

    fun isPackageRoot(dir: VirtualFile): Boolean =
        (hasLakefile(dir) || dir.findChild(LEAN_TOOLCHAIN) != null) && !isInsideLakePackages(dir)

    private fun isInsideLakePackages(dir: VirtualFile): Boolean {
        var ancestor = dir.parent
        while (ancestor != null) {
            if (ancestor.name == "packages" && ancestor.parent?.name == ".lake") return true
            ancestor = ancestor.parent
        }
        return false
    }
}
