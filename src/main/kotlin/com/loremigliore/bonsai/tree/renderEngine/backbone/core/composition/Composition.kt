package com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.Component
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import javax.swing.JComponent

class Composition(
    private val swingComponent: JComponent,
    var rootComponent: Component,
) {
    init {
        attach(rootComponent)
    }

    private fun attach(component: Component) {
        component.invalidateListener = { swingComponent.repaint() }
        if (component is ContainerComponent) {
            component.children.forEach { attach(it) }
        }
    }

    fun setRoot(newRoot: Component) {
        rootComponent = newRoot
        attach(rootComponent)
        swingComponent.repaint()
    }

    fun render(graphics: Graphics2D) {
        rootComponent.layoutIfNeeded(Constraints.Infinite, 0, 0)
        rootComponent.draw(graphics)
    }

    private fun toCanvasCoordinate(
        screenCoord: Int,
        scale: Float,
        offset: Float,
    ) = (screenCoord / scale - offset).toInt()

    private fun toCanvasX(
        screenX: Int,
        scale: Float,
        offsetX: Float,
    ) = toCanvasCoordinate(screenX, scale, offsetX)

    private fun toCanvasY(
        screenY: Int,
        scale: Float,
        offsetY: Float,
    ) = toCanvasCoordinate(screenY, scale, offsetY)

    fun dispatchClick(
        e: MouseEvent,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ): Boolean {
        rootComponent.layoutIfNeeded(Constraints.Infinite, 0, 0)
        return rootComponent.handleMouseClick(toCanvasX(e.x, scale, offsetX), toCanvasY(e.y, scale, offsetY), e)
    }

    fun dispatchMove(
        screenX: Int,
        screenY: Int,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        rootComponent.layoutIfNeeded(Constraints.Infinite, 0, 0)
        rootComponent.handleMouseMove(toCanvasX(screenX, scale, offsetX), toCanvasY(screenY, scale, offsetY))
    }
}
