package com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.Component
import java.awt.Graphics2D
import java.awt.event.MouseEvent

abstract class ContainerComponent : Component() {
    private val _children = mutableListOf<Component>()
    val children: List<Component> get() = _children

    fun addChild(child: Component) {
        child.parent = this
        _children.add(child)
    }

    protected fun drawChildren(graphics: Graphics2D) {
        for (child in _children) {
            child.draw(graphics)
        }
    }

    override fun handleMouseClick(
        x: Int,
        y: Int,
        e: MouseEvent,
    ): Boolean {
        for (child in _children.asReversed()) {
            if (child.handleMouseClick(x, y, e)) return true
        }
        return super.handleMouseClick(x, y, e)
    }

    override fun handleMouseMove(
        x: Int,
        y: Int,
    ): Boolean {
        var anyHit = false
        for (child in _children.asReversed()) {
            if (child.handleMouseMove(x, y)) anyHit = true
        }
        super.handleMouseMove(x, y)
        return anyHit
    }
}
