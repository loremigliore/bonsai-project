package com.loremigliore.bonsai.ui

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.tree.viewmodel.TreeCanvasViewModel
import com.loremigliore.bonsai.ui.components.CanvasToolbar
import com.loremigliore.bonsai.ui.components.Minimap
import com.loremigliore.bonsai.ui.components.TreeCanvas
import com.loremigliore.bonsai.ui.coordinator.VisualizerCoordinator
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JComponent
import javax.swing.JLayeredPane

class WindowContent(
    val project: Project,
) {
    private val viewModel = TreeCanvasViewModel(project)
    internal val treeCanvas = TreeCanvas(project, viewModel)
    private val canvasToolbar = CanvasToolbar(project, viewModel, project.visualizerState.cs)
    private val minimap = Minimap(project, treeCanvas)
    private val coordinator = VisualizerCoordinator(project, viewModel, treeCanvas, minimap)

    val component: JComponent =
        object : JLayeredPane() {
            init {
                layout = null

                add(treeCanvas.component)
                setLayer(treeCanvas.component, DEFAULT_LAYER)

                add(canvasToolbar.component)
                setLayer(canvasToolbar.component, PALETTE_LAYER)

                add(minimap.component)
                setLayer(minimap.component, MODAL_LAYER)

                addComponentListener(
                    object : ComponentAdapter() {
                        override fun componentResized(e: ComponentEvent?) {
                            relayout()
                            project.canvasAction.recenter(fitZoom = false, resetVertical = false)
                        }
                    },
                )

                coordinator.start(project.visualizerState.cs)
            }

            override fun doLayout() {
                super.doLayout()
                relayout()
            }

            private fun relayout() {
                treeCanvas.component.setBounds(0, 0, width, height)
                canvasToolbar.component.setBounds(
                    0,
                    0,
                    width,
                    canvasToolbar.component.preferredSize.height
                        .coerceAtLeast(44),
                )
                minimap.component.bounds = minimap.getBounds(width, height)
            }
        }
}
