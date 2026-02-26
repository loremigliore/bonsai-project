package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JPanel

class Minimap(
    val project: Project,
    private val treeCanvas: TreeCanvas,
) {
    companion object {
        private const val MINIMAP_WIDTH = BonsaiTheme.Dimensions.MINIMAP_WIDTH
        private const val MINIMAP_HEIGHT = BonsaiTheme.Dimensions.MINIMAP_HEIGHT
        private const val MINIMAP_MARGIN = BonsaiTheme.Dimensions.MINIMAP_MARGIN
        private const val MINIMAP_INNER_PADDING = BonsaiTheme.Dimensions.MINIMAP_INNER_PADDING
    }

    private var cachedSnapshot: BufferedImage? = null
    private var cachedSnapshotKey: Int = -1

    val component: JPanel by lazy {
        object : JPanel() {
            init {
                preferredSize = Dimension(MINIMAP_WIDTH, MINIMAP_HEIGHT)
                isOpaque = false
                val adapter =
                    object : MouseAdapter() {
                        override fun mousePressed(e: MouseEvent) = navigate(e.point)

                        override fun mouseDragged(e: MouseEvent) = navigate(e.point)
                    }
                addMouseListener(adapter)
                addMouseMotionListener(adapter)
            }

            override fun getPreferredSize(): Dimension = Dimension(MINIMAP_WIDTH, MINIMAP_HEIGHT)

            override fun paintComponent(g: Graphics) {
                val g2 = g as? Graphics2D ?: return
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

                val clipShape = RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), 16f, 16f)
                g2.clip = clipShape
                g2.color = UIUtil.getPanelBackground()
                g2.fill(clipShape)

                val worldBounds = computeWorldBounds()
                if (worldBounds != null) {
                    val scale = computeScale(worldBounds)
                    val tx = (MINIMAP_WIDTH - worldBounds.width * scale) / 2.0
                    val ty = (MINIMAP_HEIGHT - worldBounds.height * scale) / 2.0

                    g2.drawImage(getOrBuildSnapshot(worldBounds), 0, 0, MINIMAP_WIDTH, MINIMAP_HEIGHT, null)

                    drawViewport(g2, worldBounds, scale, tx, ty)
                }

                g2.color = BonsaiTheme.Colors.border
                g2.draw(clipShape)
            }
        }
    }

    fun invalidateSnapshot() {
        cachedSnapshot = null
        cachedSnapshotKey = -1
        component.repaint()
    }

    fun getBounds(
        parentW: Int,
        parentH: Int,
    ): Rectangle =
        Rectangle(
            parentW - MINIMAP_WIDTH - MINIMAP_MARGIN,
            parentH - MINIMAP_HEIGHT - MINIMAP_MARGIN,
            MINIMAP_WIDTH,
            MINIMAP_HEIGHT,
        )

    private fun getOrBuildSnapshot(worldBounds: Rectangle): BufferedImage {
        val key = Objects.hash(worldBounds.x, worldBounds.y, worldBounds.width, worldBounds.height)
        val existing = cachedSnapshot
        if (existing != null && cachedSnapshotKey == key) return existing

        val scale = computeScale(worldBounds)
        val tx = (MINIMAP_WIDTH - worldBounds.width * scale) / 2.0
        val ty = (MINIMAP_HEIGHT - worldBounds.height * scale) / 2.0

        val renderWidth = MINIMAP_WIDTH * 2
        val renderHeight = MINIMAP_HEIGHT * 2

        val full =
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .defaultConfiguration
                .createCompatibleImage(renderWidth, renderHeight)

        val ig2 = full.createGraphics()
        try {
            ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            ig2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            ig2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            ig2.color = UIUtil.getPanelBackground()
            ig2.fillRect(0, 0, renderWidth, renderHeight)

            ig2.scale(2.0, 2.0)
            ig2.translate(tx, ty)
            ig2.scale(scale, scale)
            ig2.translate(-worldBounds.x.toDouble(), -worldBounds.y.toDouble())
            treeCanvas.composition?.rootComponent?.draw(ig2)
        } finally {
            ig2.dispose()
        }

        cachedSnapshot = full
        cachedSnapshotKey = key
        return full
    }

    private fun navigate(point: Point) {
        val worldBounds = computeWorldBounds() ?: return
        val scale = computeScale(worldBounds)
        val tx = (MINIMAP_WIDTH - worldBounds.width * scale) / 2.0
        val ty = (MINIMAP_HEIGHT - worldBounds.height * scale) / 2.0

        val worldX = worldBounds.x + (point.x - tx) / scale
        val worldY = worldBounds.y + (point.y - ty) / scale

        val zoom = treeCanvas.viewModel.zoomState.value
        project.canvasAction.setOffset(
            (treeCanvas.component.width / 2f / zoom.scale) - worldX.toFloat(),
            (treeCanvas.component.height / 2f / zoom.scale) - worldY.toFloat(),
        )
        component.parent?.repaint()
    }

    private fun computeWorldBounds(): Rectangle? {
        val b = treeCanvas.composition?.rootComponent?.bounds ?: return null
        return if (b.width > 0 && b.height > 0) Rectangle(b.x, b.y, b.width, b.height) else null
    }

    private fun computeScale(bounds: Rectangle): Double =
        minOf(
            (MINIMAP_WIDTH - MINIMAP_INNER_PADDING * 2).toDouble() / bounds.width,
            (MINIMAP_HEIGHT - MINIMAP_INNER_PADDING * 2).toDouble() / bounds.height,
        )

    private fun drawViewport(
        g2: Graphics2D,
        worldBounds: Rectangle,
        scale: Double,
        tx: Double,
        ty: Double,
    ) {
        val zoom = treeCanvas.viewModel.zoomState.value
        g2.color = BonsaiTheme.Colors.minimapViewport
        g2.fillRect(
            (((-zoom.offsetX) - worldBounds.x) * scale + tx).toInt(),
            (((-zoom.offsetY) - worldBounds.y) * scale + ty).toInt(),
            (treeCanvas.component.width / zoom.scale * scale).toInt(),
            (treeCanvas.component.height / zoom.scale * scale).toInt(),
        )
    }
}
