package com.github.wpm.lean.infoview

import com.github.wpm.lean.LeanBundle

data class InfoviewMessage(val severity: String, val text: String)

/** Everything the infoview shows for one caret position. */
data class InfoviewModel(
    /** `File.lean:12:5`, or empty when no Lean file is selected. */
    val header: String,
    /** A status-only message (server not running, no file, error). */
    val status: String?,
    val goals: List<String>,
    /** Whether the caret is inside a tactic block at all. */
    val hasTacticState: Boolean,
    val termGoal: String?,
    val messages: List<InfoviewMessage>,
    val processing: Boolean,
) {
    fun statusLine(): String = when {
        status != null -> status
        processing -> LeanBundle.message("infoview.status.processing")
        else -> LeanBundle.message("infoview.status.ready")
    }

    /** Plain text body; section titles are Lean line comments so the Lean highlighter dims them. */
    fun renderBody(): String {
        if (status != null) return ""
        return buildString {
            append("-- ").append(LeanBundle.message("infoview.section.goals"))
            if (hasTacticState) {
                if (goals.isEmpty()) {
                    append(": ").append(LeanBundle.message("infoview.goals.accomplished")).append('\n')
                } else {
                    append(": ").append(LeanBundle.message("infoview.goals.count", goals.size)).append('\n')
                    for (goal in goals) {
                        append('\n').append(goal.trimEnd()).append('\n')
                    }
                }
            } else {
                append(": ").append(LeanBundle.message("infoview.no.tactic.state")).append('\n')
            }
            if (!termGoal.isNullOrBlank()) {
                append('\n').append("-- ").append(LeanBundle.message("infoview.section.expected.type")).append('\n')
                append(termGoal.trimEnd()).append('\n')
            }
            append('\n').append("-- ").append(LeanBundle.message("infoview.section.messages"))
            if (messages.isEmpty()) {
                append(": ").append(LeanBundle.message("infoview.no.messages")).append('\n')
            } else {
                append('\n')
                for (message in messages) {
                    append('\n').append('[').append(message.severity).append("] ").append(message.text.trimEnd()).append('\n')
                }
            }
        }
    }

    companion object {
        fun idle(status: String, header: String = ""): InfoviewModel =
            InfoviewModel(header, status, emptyList(), false, null, emptyList(), false)
    }
}
