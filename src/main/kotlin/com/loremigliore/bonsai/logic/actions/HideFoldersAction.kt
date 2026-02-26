package com.loremigliore.bonsai.logic.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.utils.toolbarAction
import com.loremigliore.bonsai.ui.icons.PluginIcons

class HideFoldersAction :
    ToggleAction(
        BonsaiBundle.message("action.hide.folders.text"),
        BonsaiBundle.message("action.hide.folders.description"),
        PluginIcons.FoldersIconShow,
    ) {
    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        return project.toolbarAction.state.value.hideFolders
    }

    override fun setSelected(
        e: AnActionEvent,
        state: Boolean,
    ) {
        val project = e.project ?: return
        project.toolbarAction.setFoldersHide(state)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project ?: return

        e.presentation.icon =
            if (project.toolbarAction.state.value.hideFolders) {
                PluginIcons.FoldersIconHide
            } else {
                PluginIcons.FoldersIconShow
            }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}
