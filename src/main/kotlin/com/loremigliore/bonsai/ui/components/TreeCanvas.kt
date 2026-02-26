package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.logic.utils.searchAction
import com.loremigliore.bonsai.tree.renderEngine.RenderEngine
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.Composition
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.viewmodel.TreeCanvasViewModel
import com.loremigliore.bonsai.ui.components.utils.MouseHandler
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import javax.swing.JPanel

class TreeCanvas(
    val project: Project,
    internal val viewModel: TreeCanvasViewModel,
) {
    internal var composition: Composition? = null

    internal var lastRegistries: List<ComponentRegistry> = emptyList()

    private val mouseHandler =
        MouseHandler(
            onClick = { e ->
                val zoom = viewModel.zoomState.value
                val hit = composition?.dispatchClick(e, zoom.scale, zoom.offsetX, zoom.offsetY) ?: false
                if (!hit) viewModel.onEmptyClicked(e)
            },
            onDrag = { dx, dy -> project.canvasAction.onDrag(dx, dy) },
            onMouseWheel = { e -> project.canvasAction.onMouseWheelMoved(e) },
            onMove = { e ->
                val zoom = viewModel.zoomState.value
                composition?.dispatchMove(e.x, e.y, zoom.scale, zoom.offsetX, zoom.offsetY)
            },
        )

    val component: JPanel =
        object : JPanel() {
            init {
                background = JBColor.background()
                isOpaque = true
                isFocusable = true
                mouseHandler.installOn(this)

                addComponentListener(
                    object : ComponentAdapter() {
                        override fun componentResized(e: ComponentEvent?) {
                            val comp = composition ?: return
                            val b = comp.rootComponent.bounds
                            if (b.width > 0 && b.height > 0) {
                                project.canvasAction.fitToContent(
                                    width,
                                    height,
                                    b.width,
                                    b.height,
                                    fitZoom = false,
                                    resetVertical = false,
                                )
                            }
                        }
                    },
                )
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2 = g as? Graphics2D ?: return
                val comp = composition ?: return

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                val zoom = viewModel.zoomState.value
                g2.scale(zoom.scale.toDouble(), zoom.scale.toDouble())
                g2.translate(zoom.offsetX.toDouble(), zoom.offsetY.toDouble())

                comp.render(g2)
            }
        }

    fun renderTree(canvasState: CanvasState) {
        component.revalidate()
        project.searchAction.clearMatches()
        val result = RenderEngine.buildTree(canvasState, project, viewModel::onItemClicked)
        lastRegistries = result.registries
        val existing = composition
        if (existing == null) {
            composition = Composition(component, result.rootComponent)
        } else {
            existing.setRoot(result.rootComponent)
        }
        component.repaint()
    }

    fun handleZoomChange() {
        component.repaint()
    }

    fun scrollToItem(id: Int) {
        val component =
            lastRegistries.firstNotNullOfOrNull { it.resolve(-id) }
                ?: lastRegistries.firstNotNullOfOrNull { it.resolve(id) }
                ?: return
        val b = component.bounds
        if (b.width <= 0 || b.height <= 0) return

        val zoom = viewModel.zoomState.value
        val canvasW = this.component.width
        val canvasH = this.component.height

        val newOffsetX = (canvasW / 2f / zoom.scale) - b.x - b.width / 2f
        val newOffsetY = (canvasH / 2f / zoom.scale) - b.y - b.height / 2f
        project.canvasAction.setOffset(newOffsetX, newOffsetY)
    }

    fun exportAsImage(): BufferedImage? {
        val comp = composition ?: return null

        comp.rootComponent.layoutIfNeeded(Constraints.Infinite, 0, 0)

        val rootBounds = comp.rootComponent.bounds
        val width = rootBounds.width
        val height = rootBounds.height
        if (width <= 0 || height <= 0) return null

        val image =
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .defaultConfiguration
                .createCompatibleImage(width, height, java.awt.Transparency.TRANSLUCENT)

        val g2 = image.createGraphics()
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            g2.color = component.background
            g2.fillRect(0, 0, width, height)

            g2.translate(-rootBounds.x, -rootBounds.y)

            comp.render(g2)
        } finally {
            g2.dispose()
        }

        return image
    }
}
