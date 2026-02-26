package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.ui.theme.DrawUtils
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.awt.EventQueue
import javax.swing.BoxLayout
import javax.swing.JPanel

class TreeDetailSegmentedButton(
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
            .map { it.treeDetail }
            .distinctUntilChanged()
            .onEach { selected ->
                EventQueue.invokeLater {
                    rebuild(selected)
                }
            }.launchIn(project.visualizerState.cs)
    }

    private fun rebuild(selected: TreeDetail) {
        panel.removeAll()

        TreeDetail.entries.forEach { option ->
            val key = "tree.detail.${option.name.lowercase()}"
            val label = BonsaiBundle.message(key)

            val button =
                DrawUtils.CustomSegmentedButtonItem(
                    label,
                    selected == option,
                ) {
                    project.headerAction.setTreeDetail(option)
                }

            panel.add(button)
        }

        panel.revalidate()
        panel.repaint()
    }
}
