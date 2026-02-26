package com.loremigliore.bonsai.ui

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.ui.components.DepthSlider
import com.loremigliore.bonsai.ui.components.OptionsDropdown
import com.loremigliore.bonsai.ui.components.TreeCanvas
import com.loremigliore.bonsai.ui.components.TreeDetailSegmentedButton
import com.loremigliore.bonsai.ui.components.TreeLayoutSegmentedButton
import com.loremigliore.bonsai.ui.components.utils.SpaceBetweenWrapLayoutUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class WindowHeader(
    project: Project,
    treeCanvas: TreeCanvas? = null,
) {
    private val optionsDropdown = OptionsDropdown(project, treeCanvas)
    private val depthSlider = DepthSlider(project)

    private val treeDetailSegmentedButton = TreeDetailSegmentedButton(project)
    private val treeLayoutSegmentedButton = TreeLayoutSegmentedButton(project)

    val component: JComponent =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.X_AXIS)
                    border = BorderFactory.createEmptyBorder(8, 8, 4, 8)

                    add(optionsDropdown.component)
                    add(Box.createHorizontalGlue())
                    add(depthSlider.component)
                },
            )

            add(
                JPanel().apply {
                    layout = SpaceBetweenWrapLayoutUtils()
                    border = BorderFactory.createEmptyBorder(4, 8, 8, 8)

                    add(treeDetailSegmentedButton.component, 0.0f)
                    add(treeLayoutSegmentedButton.component, 1.0f)

                    addComponentListener(
                        object : ComponentAdapter() {
                            override fun componentResized(e: ComponentEvent?) {
                                revalidate()
                                repaint()
                            }
                        },
                    )
                },
            )

            project.headerAction.state
                .onEach { state ->
                    depthSlider.max = state.maxDepth
                    if (depthSlider.value != state.currentDepth) {
                        depthSlider.value = state.currentDepth
                    }
                }.launchIn(project.visualizerState.cs)
        }
}
