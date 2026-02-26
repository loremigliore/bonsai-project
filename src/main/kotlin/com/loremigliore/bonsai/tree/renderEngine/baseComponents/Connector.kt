package com.loremigliore.bonsai.tree.renderEngine.baseComponents

import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.Component
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Bounds
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.BasicStroke
import java.awt.Graphics2D

class Connector(
    private val item: TreeItem,
    private val layoutMode: TreeLayout,
    private val maxDepth: Int,
    private val resolveComponent: (Int) -> Component?,
    private val resolveTitle: (Int) -> Component? = { null },
) : Component() {
    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY
        return Size(0, 0)
    }

    override fun draw(graphics: Graphics2D) {
        val parentBounds = resolveTitle(item.id)?.bounds ?: bounds

        item.children
            .filter { it.depth <= maxDepth }
            .forEach { child ->
                val childComponent = resolveComponent(child.id) ?: return@forEach
                val childTitle = resolveTitle(child.id)

                when (layoutMode) {
                    TreeLayout.CASCADE -> {
                        if (childTitle != null) {
                            drawLConnector(graphics, parentBounds, childTitle.bounds)
                        } else {
                            drawLConnector(graphics, parentBounds, childComponent.bounds)
                        }
                    }

                    TreeLayout.DIAGRAM -> {
                        drawSConnector(graphics, parentBounds, childComponent.bounds)
                    }
                }
            }
    }

    private fun drawLConnector(
        g: Graphics2D,
        parent: Bounds,
        child: Bounds,
    ) {
        val startX = parent.x + parent.width / 2
        val startY = parent.y + parent.height + BonsaiTheme.Spacing.treeConnectionVerticalSpacing
        val endX = child.x
        val endY = child.y + child.height / 2

        g.color = BonsaiTheme.Colors.treeConnection
        g.stroke = BasicStroke(BonsaiTheme.Spacing.treeConnectionStroke)

        g.drawLine(startX, startY, startX, endY)
        g.drawLine(startX, endY, endX, endY)
    }

    private fun drawSConnector(
        g: Graphics2D,
        parent: Bounds,
        child: Bounds,
    ) {
        val startX = parent.x + parent.width / 2
        val startY = parent.y + parent.height + BonsaiTheme.Spacing.treeConnectionVerticalSpacing
        val endX = child.x + child.width / 2
        val endY = child.y

        val midY =
            startY + ((endY - startY) * BonsaiTheme.Layout.TREE_CONNECTION_SPLIT_RATIO).toInt()

        g.color = BonsaiTheme.Colors.treeConnection
        g.stroke = BasicStroke(BonsaiTheme.Spacing.treeConnectionStroke)

        g.drawLine(startX, startY, startX, midY)
        g.drawLine(startX, midY, endX, midY)
        g.drawLine(endX, midY, endX, endY)
    }
}
