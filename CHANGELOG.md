# Changelog

## [Unreleased]

### Added

- Lean 4 file type with lexer-based syntax highlighting and a color settings page
- Language server integration through the native LSP API (`lake serve` / `lean --server`)
- Semantic highlighting from the server, including `sorry`-like terms
- Lean Infoview tool window: tactic goals, expected type, and messages at the caret
- Unicode abbreviation input (`\alpha` → `α`) with the VS Code abbreviation table
- Brace matching, comment toggling, settings page, and a Restart Lean Server action
