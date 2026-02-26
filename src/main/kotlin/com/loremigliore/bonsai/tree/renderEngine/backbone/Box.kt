package com.loremigliore.bonsai.tree.renderEngine.backbone

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.ContainerComponent
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import java.awt.BasicStroke
import java.awt.Graphics2D

open class Box : ContainerComponent() {
    var borderRadius: Int = 0
        set(value) {
            if (field != value) {
                field = value
                requestRender()
            }
        }

    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY

        var maxWidth = 0
        var maxHeight = 0

        val contentX = offsetX + padding.left
        val contentY = offsetY + padding.top

        for (child in children) {
            child.layoutIfNeeded(constraints, contentX + child.margin.left, contentY + child.margin.top)
            maxWidth = maxOf(maxWidth, child.bounds.width + child.margin.left + child.margin.right)
            maxHeight = maxOf(maxHeight, child.bounds.height + child.margin.top + child.margin.bottom)
        }

        bounds.width = maxWidth + padding.left + padding.right
        bounds.height = maxHeight + padding.top + padding.bottom

        return Size(bounds.width, bounds.height)
    }

    override fun draw(graphics: Graphics2D) {
        backgroundColor?.let {
            graphics.color = it
            if (borderRadius > 0) {
                graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, borderRadius, borderRadius)
            } else {
                graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
            }
        }
        if (borderColor != null && borderWidth > 0) {
            graphics.color = borderColor
            graphics.stroke = BasicStroke(borderWidth.toFloat())
            if (borderRadius > 0) {
                graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, borderRadius, borderRadius)
            } else {
                graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height)
            }
        }
        drawChildren(graphics)
    }
}
