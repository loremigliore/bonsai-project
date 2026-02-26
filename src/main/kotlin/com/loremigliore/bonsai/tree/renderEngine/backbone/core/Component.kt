package com.loremigliore.bonsai.tree.renderEngine.backbone.core

import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.State
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Bounds
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.event.MouseEvent

abstract class Component {
    val bounds: Bounds = Bounds()

    internal var parent: Component? = null
    internal var invalidateListener: (() -> Unit)? = null

    var onLayoutComplete: (() -> Unit)? = null

    private var layoutDirty = true

    var backgroundColor: Color? = null
        set(value) {
            if (field != value) {
                field = value
                markRenderDirty()
            }
        }

    var borderColor: Color? = null
        set(value) {
            if (field != value) {
                field = value
                markRenderDirty()
            }
        }

    var borderWidth: Int = 0
        set(value) {
            if (field != value) {
                field = value
                markRenderDirty()
            }
        }

    var padding: Insets = JBUI.emptyInsets()
        set(value) {
            if (field != value) {
                field = value
                markLayoutDirty()
            }
        }

    var margin: Insets = JBUI.emptyInsets()
        set(value) {
            if (field != value) {
                field = value
                markLayoutDirty()
            }
        }

    var onClick: ((MouseEvent) -> Unit)? = null
    var onHoverEnter: (() -> Unit)? = null
    var onHoverExit: (() -> Unit)? = null

    private var isHovered = false

    protected fun observes(vararg states: State<*>) {
        states.forEach { it.observe { markRenderDirty() } }
    }

    private fun markRenderDirty() {
        invalidateListener?.invoke()
    }

    private fun markLayoutDirty() {
        layoutDirty = true
        parent?.onChildLayoutInvalidated(this)
        invalidateListener?.invoke()
    }

    internal fun requestRender() = markRenderDirty()

    open fun onChildLayoutInvalidated(child: Component) {
        markLayoutDirty()
    }

    internal fun layoutIfNeeded(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size =
        if (layoutDirty) {
            val size = layout(constraints, offsetX, offsetY)
            layoutDirty = false
            onLayoutComplete?.invoke()
            onLayoutComplete = null
            size
        } else {
            Size(bounds.width, bounds.height)
        }

    abstract fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size

    abstract fun draw(graphics: Graphics2D)

    open fun handleMouseClick(
        x: Int,
        y: Int,
        e: MouseEvent,
    ): Boolean {
        if (bounds.contains(x, y)) {
            onClick?.let {
                it.invoke(e)
                return true
            }
        }
        return false
    }

    open fun handleMouseMove(
        x: Int,
        y: Int,
    ): Boolean {
        val inside = bounds.contains(x, y)
        if (inside && !isHovered) {
            isHovered = true
            onHoverEnter?.invoke()
            markRenderDirty()
        } else if (!inside && isHovered) {
            isHovered = false
            onHoverExit?.invoke()
            markRenderDirty()
        }
        return inside
    }

    protected fun drawBackgroundAndBorder(graphics: Graphics2D) {
        backgroundColor?.let {
            graphics.color = it
            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
        }
        if (borderColor != null && borderWidth > 0) {
            graphics.color = borderColor
            graphics.stroke = BasicStroke(borderWidth.toFloat())
            graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height)
        }
    }
}
