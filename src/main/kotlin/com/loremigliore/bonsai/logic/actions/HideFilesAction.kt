package com.loremigliore.bonsai.logic.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.utils.toolbarAction
import com.loremigliore.bonsai.ui.icons.PluginIcons

class HideFilesAction :
    ToggleAction(
        BonsaiBundle.message("action.hide.files.text"),
        BonsaiBundle.message("action.hide.files.description"),
        PluginIcons.FilesIconShow,
    ) {
    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        return project.toolbarAction.state.value.hideFiles
    }

    override fun setSelected(
        e: AnActionEvent,
        state: Boolean,
    ) {
        val project = e.project ?: return
        project.toolbarAction.setFilesHide(state)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project ?: return

        e.presentation.icon =
            if (project.toolbarAction.state.value.hideFiles) {
                PluginIcons.FilesIconHide
            } else {
                PluginIcons.FilesIconShow
            }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}
