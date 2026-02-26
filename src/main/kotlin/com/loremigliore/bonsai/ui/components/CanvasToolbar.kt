package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.logic.utils.canvasToolbarAction
import com.loremigliore.bonsai.tree.viewmodel.TreeCanvasViewModel
import com.loremigliore.bonsai.ui.components.prototypes.CanvasButton
import com.loremigliore.bonsai.ui.components.prototypes.Searchbar
import com.loremigliore.bonsai.ui.components.utils.SpaceBetweenWrapLayoutUtils
import com.loremigliore.bonsai.ui.icons.PluginIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities

class CanvasToolbar(
    val project: Project,
    val viewModel: TreeCanvasViewModel,
    val scope: CoroutineScope,
) {
    private val reset = CanvasButton(PluginIcons.Reset) { viewModel.unfocus() }
    private val recenter =
        CanvasButton(PluginIcons.Recenter) {
            project.canvasAction.recenter(
                fitZoom = true,
                resetVertical = true,
            )
        }
    private val searchbar = Searchbar(project)

    val component =
        JPanel().apply {
            isOpaque = false
            layout = SpaceBetweenWrapLayoutUtils()
            border = BorderFactory.createEmptyBorder(0, 10, 10, 10)
            add(reset.component, 0.0f)
            add(searchbar.component, 0.5f)
            add(recenter.component, 1.0f)
            addComponentListener(
                object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent?) {
                        revalidate()
                        repaint()
                    }
                },
            )
        }

    init {
        setupButtonVisibility()
    }

    private fun setupButtonVisibility() {
        viewModel.isPanned
            .onEach { project.canvasToolbarAction.setShowRecenter(it) }
            .launchIn(scope)

        project.canvasAction.state
            .onEach { project.canvasToolbarAction.setShowReset(it.focusedSubtree.isNotEmpty()) }
            .launchIn(scope)

        project.canvasToolbarAction.state
            .onEach { state ->
                reset.component.isVisible = state.showReset
                recenter.component.isVisible = state.showRecenter
                searchbar.component.isVisible = state.showSearchbar
                if (state.showSearchbar) {
                    SwingUtilities.invokeLater { searchbar.requestFocus() }
                }
                component.revalidate()
                component.repaint()
            }.launchIn(scope)
    }
}
