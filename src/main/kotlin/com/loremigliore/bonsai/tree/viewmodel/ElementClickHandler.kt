package com.loremigliore.bonsai.tree.viewmodel

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager

object ElementClickHandler {
    fun openFileInEditor(
        filePath: String,
        project: Project,
    ) {
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return

        ToolWindowManager.getInstance(project).getToolWindow("Project")?.show()
        ProjectView.getInstance(project).select(null, virtualFile, false)

        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }

    fun selectInProjectView(
        filePath: String,
        project: Project,
    ) {
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return

        ToolWindowManager.getInstance(project).getToolWindow("Project")?.show()
        ProjectView.getInstance(project).select(null, virtualFile, true)
    }
}
