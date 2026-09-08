package com.github.wpm.lean.lsp

import com.github.wpm.lean.LeanBundle
import com.github.wpm.lean.settings.LeanSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.SystemProperties
import com.intellij.util.execution.ParametersListUtil
import java.nio.file.Files
import java.nio.file.Path

/** Finds the Lean toolchain binaries and builds the language server command line. */
object LeanToolchain {
    /**
     * Resolution order: the directory configured in settings (if set, it must contain the binary),
     * then the `PATH`, then the default elan location `~/.elan/bin`.
     */
    fun findExecutable(name: String, configuredDir: String?): Path? {
        val fileName = if (SystemInfo.isWindows) "$name.exe" else name
        val dir = configuredDir?.trim().orEmpty()
        if (dir.isNotEmpty()) {
            val candidate = Path.of(FileUtil.expandUserHome(dir)).resolve(fileName)
            return candidate.takeIf { Files.isExecutable(it) }
        }
        PathEnvironmentVariableUtil.findInPath(fileName)?.let { return it.toPath() }
        val elan = Path.of(SystemProperties.getUserHome(), ".elan", "bin", fileName)
        return elan.takeIf { Files.isExecutable(it) }
    }

    /**
     * `lake serve` inside a Lake package (so dependencies and the package's own `lean-toolchain`
     * are honoured), `lean --server` for a bare directory. The elan proxies pick the toolchain
     * from `lean-toolchain` in the working directory.
     */
    @Throws(ExecutionException::class)
    fun createServerCommandLine(root: VirtualFile, settings: LeanSettings.State): GeneralCommandLine {
        val useLake = LeanProjectRoots.hasLakefile(root)
        val executableName = if (useLake) "lake" else "lean"
        val executable = findExecutable(executableName, settings.toolchainDir)
            ?: throw ExecutionException(LeanBundle.message("lsp.executable.not.found", executableName))

        val commandLine = GeneralCommandLine(executable.toString())
        if (useLake) {
            commandLine.addParameters("serve", "--")
        } else {
            commandLine.addParameter("--server")
        }
        settings.serverArgs?.takeIf { it.isNotBlank() }?.let { commandLine.addParameters(ParametersListUtil.parse(it)) }
        // The VS Code extension passes the folder too, so the server shows up nicely in `ps`.
        commandLine.addParameter(root.path)
        commandLine.withWorkDirectory(root.path)
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        return commandLine
    }
}
