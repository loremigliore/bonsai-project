package com.loremigliore.bonsai.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.loremigliore.bonsai.ui.WindowContent
import com.loremigliore.bonsai.ui.WindowHeader

class ProjectVisualToolWindowContent(
    project: Project,
) {
    private val windowContent = WindowContent(project)
    private val windowHeader = WindowHeader(project, windowContent.treeCanvas)

    val windowPanel =
        SimpleToolWindowPanel(true, true).apply {
            toolbar = windowHeader.component
            setContent(windowContent.component)
        }
}
