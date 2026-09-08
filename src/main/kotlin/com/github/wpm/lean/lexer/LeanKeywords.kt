package com.github.wpm.lean.lexer

/**
 * Word lists used to classify identifiers while lexing.
 *
 * The keyword list follows the TextMate grammar shipped with the official VS Code
 * extension (`vscode-lean4/syntaxes/lean4.json`), extended with common tactic names.
 * It is deliberately permissive: the lexer only drives baseline syntax highlighting,
 * while precise classification comes from the language server's semantic tokens.
 */
object LeanKeywords {
    val KEYWORDS: Set<String> = setOf(
        "theorem", "show", "have", "using", "haveI", "from", "suffices", "nomatch", "nofun", "no_index",
        "def", "class", "structure", "instance", "elab", "set_option", "initialize", "builtin_initialize",
        "example", "inductive_fixpoint", "inductive", "coinductive_fixpoint", "coinductive",
        "termination_by?", "termination_by", "decreasing_by", "partial_fixpoint", "axiom", "universe",
        "variable", "module", "import", "open", "export", "prelude", "renaming", "hiding", "do", "by?", "by",
        "let", "letI", "let_expr", "extends", "mutual", "mut", "where", "rec", "declare_syntax_cat", "syntax",
        "macro_rules", "macro", "max_prec", "leading_parser", "elab_rules", "deriving", "fun", "section",
        "namespace", "end", "prefix", "postfix", "infixl", "infixr", "infix", "notation", "abbrev", "if", "bif",
        "then", "else", "calc", "matches", "match_expr", "match", "with", "forall", "for", "while", "repeat",
        "unless", "until", "panic!", "unreachable!", "assert!", "try", "catch", "finally", "return", "continue",
        "break", "exists", "mod_cast", "exact?", "include_str", "include", "in", "trailing_parser", "tactic_tag",
        "tactic_alt", "tactic_extension", "register_tactic_tag", "binder_predicate", "grind_propagator",
        "builtin_grind_propagator", "grind_pattern", "simproc", "builtin_simproc", "simproc_decl",
        "builtin_simproc_decl", "dsimproc", "builtin_dsimproc", "dsimproc_decl", "builtin_dsimproc_decl",
        "show_panel_widgets", "show_term", "seal", "unseal", "nat_lit", "norm_cast_add_elim", "println!",
        "declare_config_elab", "register_error_explanation", "register_builtin_option", "register_option",
        "register_parser_alias", "register_simp_attr", "register_linter_set", "register_label_attr",
        "recommended_spelling", "reportIssue!", "reprove", "run_elab", "run_cmd", "run_meta", "add_decl_doc",
        "omit", "opaque", "dbg_trace", "throwErrorAt", "throwError", "throwNamedErrorAt", "throwNamedError",
        "logNamedWarningAt", "logNamedWarning", "logNamedErrorAt", "logNamedError", "lemma", "attribute",
        "at", "only", "generalizing", "induction", "cases", "rcases", "obtain", "intro", "intros", "rintro",
        "exact", "apply", "refine", "rw", "rwa", "simp", "simp_all", "dsimp", "omega", "decide", "trivial",
        "rfl", "constructor", "use", "ext", "funext", "congr", "linarith", "nlinarith", "positivity",
        "norm_num", "ring", "field_simp", "push_neg", "contradiction", "exfalso", "assumption", "aesop",
        "tauto", "specialize", "subst", "symm", "trans", "unfold", "change", "split", "next", "all_goals",
        "any_goals", "first", "focus", "case", "done", "skip", "conv", "exact_mod_cast", "norm_cast",
        "push_cast", "left", "right", "injection", "revert", "clear", "rename_i", "set", "guard_hyp",
        "guard_target", "infer_instance", "native_decide", "simpa", "fun_prop", "gcongr", "bound", "grind",
        "cutsat", "lia", "bv_decide",
    )

    val MODIFIERS: Set<String> = setOf(
        "local", "scoped", "partial", "unsafe", "nonrec", "public", "private", "protected", "noncomputable", "meta",
    )

    val SORTS: Set<String> = setOf("Prop", "Type", "Sort")

    val SORRY_LIKE: Set<String> = setOf("sorry", "admit")

    val BOOLEANS: Set<String> = setOf("true", "false")
}
