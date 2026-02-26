package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.ui.icons.PluginIcons
import com.loremigliore.bonsai.ui.theme.DrawUtils
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class TreeLayoutSegmentedButton(
    private val project: Project,
) {
    private val panel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            border = DrawUtils.CustomRoundedLineBorder()
        }

    val component: JPanel = panel

    init {
        project.headerAction.state
            .map { it.treeLayout }
            .distinctUntilChanged()
            .onEach { selected ->
                SwingUtilities.invokeLater {
                    rebuild(selected)
                }
            }.launchIn(project.visualizerState.cs)
    }

    private fun rebuild(selected: TreeLayout) {
        panel.removeAll()

        TreeLayout.entries.forEach { option ->

            val icon =
                when (option) {
                    TreeLayout.CASCADE -> PluginIcons.TreeLayoutIconCascade
                    TreeLayout.DIAGRAM -> PluginIcons.TreeLayoutIconDiagram
                }

            val button =
                DrawUtils.CustomSegmentedButtonItem(
                    icon,
                    selected == option,
                ) {
                    project.headerAction.setTreeLayout(option)
                }

            panel.add(button)
        }

        panel.revalidate()
        panel.repaint()
    }
}
