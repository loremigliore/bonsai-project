package com.loremigliore.bonsai.tree.renderEngine.backbone

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.ContainerComponent
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import java.awt.Graphics2D

open class Column(
    private val spacing: Int = 0,
) : ContainerComponent() {
    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY

        val contentX = offsetX + padding.left
        var cursorY = offsetY + padding.top
        var maxWidth = 0

        for ((index, child) in children.withIndex()) {
            cursorY += child.margin.top
            child.layoutIfNeeded(constraints, contentX + child.margin.left, cursorY)
            cursorY += child.bounds.height + child.margin.bottom
            if (index != children.lastIndex) cursorY += spacing
            maxWidth = maxOf(maxWidth, child.bounds.width + child.margin.left + child.margin.right)
        }

        bounds.width = maxWidth + padding.left + padding.right
        bounds.height = (cursorY - offsetY) + padding.bottom

        return Size(bounds.width, bounds.height)
    }

    override fun draw(graphics: Graphics2D) {
        drawBackgroundAndBorder(graphics)
        drawChildren(graphics)
    }
}
