package com.github.wpm.lean.lsp

import com.github.wpm.lean.LeanBundle
import com.github.wpm.lean.settings.LeanConfigurable
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

object LeanNotifications {
    private const val GROUP_ID = "Lean"
    private val START_FAILURE_SHOWN = Key.create<Boolean>("com.github.wpm.lean.startFailureShown")

    /** Shown once per project so a missing toolchain does not produce a balloon per opened file. */
    fun serverStartFailed(project: Project, message: String?) {
        if (project.getUserData(START_FAILURE_SHOWN) == true) return
        project.putUserData(START_FAILURE_SHOWN, true)
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(
                LeanBundle.message("notification.server.start.failed.title"),
                message ?: "",
                NotificationType.ERROR,
            )
            .addAction(NotificationAction.createSimpleExpiring(LeanBundle.message("notification.configure")) {
                project.putUserData(START_FAILURE_SHOWN, null)
                ShowSettingsUtil.getInstance().showSettingsDialog(project, LeanConfigurable::class.java)
            })
            .notify(project)
    }
}
