package com.loremigliore.bonsai.toolWindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ProjectVisualWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val toolWindowContent = ProjectVisualToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(toolWindowContent.windowPanel, "", false)
        toolWindow.contentManager.addContent(content)

        val toggleHideFoldersAction =
            ActionManager.getInstance().getAction(
                "com.loremigliore.bonsai.logic.actions.HideFoldersAction",
            )

        val toggleHideFilesAction =
            ActionManager.getInstance().getAction(
                "com.loremigliore.bonsai.logic.actions.HideFilesAction",
            )

        val toggleHideMiddlePackagesAction =
            ActionManager.getInstance().getAction(
                "com.loremigliore.bonsai.logic.actions.HideMiddlePackagesAction",
            )

        val toggleShrinkMiddlePackagesAction =
            ActionManager.getInstance().getAction(
                "com.loremigliore.bonsai.logic.actions.ShrinkMiddlePackagesAction",
            )

        if (toggleHideFoldersAction != null && toggleHideFilesAction != null && toggleShrinkMiddlePackagesAction != null &&
            toggleHideMiddlePackagesAction != null
        ) {
            val actions: List<AnAction> =
                listOf(toggleHideFoldersAction, toggleHideFilesAction, toggleHideMiddlePackagesAction, toggleShrinkMiddlePackagesAction)
            toolWindow.setTitleActions(actions)
        }
    }
}
