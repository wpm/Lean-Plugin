# Lean 4 for JetBrains IDEs

<!-- Plugin description -->
Support for the [Lean 4](https://lean-lang.org) programming language and theorem prover, built on the
Lean language server.

- Syntax highlighting (lexer-based, refined by the server's semantic tokens)
- Diagnostics, completion, hover documentation, go to definition, find usages, and the other
  editor features the IDE derives from the Language Server Protocol
- A **Lean Infoview** tool window showing the tactic goals, expected type, and messages at the caret
- Unicode input with the same abbreviations as the VS Code extension: `\alpha` → `α`, `\to` → `→`, `\<>` → `⟨⟩`
- Brace matching for `()`, `[]`, `{}`, `⟨⟩`, `⦃⦄`, and comment toggling for `--` and `/- -/`

Requires a JetBrains IDE that ships the native LSP API (IntelliJ IDEA Ultimate, PyCharm, CLion,
GoLand, RustRover, WebStorm, …) version 2025.2.1 or later, and a Lean toolchain installed with
[elan](https://github.com/leanprover/elan).
<!-- Plugin description end -->

## Trying it out

1. Install Lean via elan so that `lake` and `lean` are on your `PATH` (or in `~/.elan/bin`).
2. Run the plugin in a sandbox IDE:

   ```shell
   ./gradlew runIde
   ```

   The first run downloads IntelliJ IDEA Ultimate 2025.2 into the Gradle cache.
3. In the sandbox, open a Lake project (a directory with `lakefile.lean` or `lakefile.toml`) and
   open a `.lean` file. The server starts automatically; its status is in the
   **Language Services** widget in the status bar.
4. Open **View | Tool Windows | Lean Infoview** and move the caret into a `by` block.

To build an installable ZIP instead:

```shell
./gradlew buildPlugin
```

The archive lands in `build/distributions/` and can be installed via
**Settings | Plugins | ⚙ | Install Plugin from Disk…**.

## How the server is chosen

For each opened `.lean` file the plugin walks up the directory tree to the nearest
`lakefile.lean`, `lakefile.toml`, or `lean-toolchain`, skipping dependency checkouts under
`.lake/packages`. One server is started per such root:

- with a lakefile: `lake serve --` in that directory, so dependencies and the package's own
  `lean-toolchain` are honoured;
- otherwise: `lean --server`.

Binaries are resolved from the directory configured in
**Settings | Languages & Frameworks | Lean 4**, then from the `PATH`, then from `~/.elan/bin`.
**Tools | Restart Lean Server** restarts every Lean server in the project.

## Unicode input

Type `\` followed by an abbreviation. It is replaced

- as soon as it is unambiguous (configurable: *Replace abbreviations eagerly*),
- when you type a character that cannot extend any abbreviation (that character is kept: `\alp7` → `α7`),
- when you press Tab, or
- when the caret leaves the abbreviation.

The table is the one from the VS Code extension (`lean4-unicode-input`); see
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Development notes

- The plugin targets the pre-2026.1.4 names of the LSP API (`LspServerSupportProvider`,
  `LspServerDescriptor`). Those classes are deprecated but kept in newer IDEs, so one build runs on
  2025.2 through 2026.x. The `since-build` is 2025.2.1, the first release exposing the
  `com.intellij.modules.lsp` module.
- Lexing and abbreviation lookup are platform-independent Kotlin classes with plain JUnit tests
  (`./gradlew test`).
- Enable `#com.intellij.platform.lsp` in **Help | Diagnostic Tools | Debug Log Settings** to see
  the LSP traffic in `idea.log`.

## Not yet done

- The interactive infoview of VS Code (widgets, clickable terms, "all messages" view). The tool
  window shows the plain-text goals from `$/lean/plainGoal` and the diagnostics on the caret line.
- Run configurations for `lake build` / `lake exe`.
- Structure view and code folding beyond what the server provides.
