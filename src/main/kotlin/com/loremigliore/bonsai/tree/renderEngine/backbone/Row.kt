package com.loremigliore.bonsai.tree.renderEngine.backbone

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.ContainerComponent
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import java.awt.Graphics2D

open class Row(
    private val spacing: Int = 0,
) : ContainerComponent() {
    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY

        val contentY = offsetY + padding.top
        var cursorX = offsetX + padding.left
        var maxHeight = 0

        for ((index, child) in children.withIndex()) {
            cursorX += child.margin.left
            child.layoutIfNeeded(constraints, cursorX, contentY + child.margin.top)
            cursorX += child.bounds.width + child.margin.right
            if (index != children.lastIndex) cursorX += spacing
            maxHeight = maxOf(maxHeight, child.bounds.height + child.margin.top + child.margin.bottom)
        }

        bounds.width = (cursorX - offsetX) + padding.right
        bounds.height = maxHeight + padding.top + padding.bottom

        return Size(bounds.width, bounds.height)
    }

    override fun draw(graphics: Graphics2D) {
        drawBackgroundAndBorder(graphics)
        drawChildren(graphics)
    }
}
