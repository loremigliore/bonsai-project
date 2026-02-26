package com.loremigliore.bonsai.ui.components.utils

import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.JPanel
import kotlin.math.abs

class MouseHandler(
    private val onClick: (MouseEvent) -> Unit,
    private val onDrag: (dx: Int, dy: Int) -> Unit,
    private val onMouseWheel: (MouseWheelEvent) -> Unit,
    private val onMove: ((MouseEvent) -> Unit)? = null,
) {
    private var pressPoint: Point? = null
    private var didDrag = false
    private var isDragging = false

    fun installOn(component: JPanel) {
        val mouseAdapter =
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    component.requestFocusInWindow()
                    if (e.button == MouseEvent.BUTTON1 || e.button == MouseEvent.BUTTON2) {
                        pressPoint = e.point
                        didDrag = false
                        isDragging = true
                    } else if (e.button == MouseEvent.BUTTON3) {
                        onClick(e)
                    }
                }

                override fun mouseReleased(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON1) {
                        if (!didDrag) onClick(e)
                        pressPoint = null
                        didDrag = false
                        isDragging = false
                    } else if (e.button == MouseEvent.BUTTON2) {
                        pressPoint = null
                        didDrag = false
                        isDragging = false
                    }
                }

                override fun mouseMoved(e: MouseEvent) {
                    onMove?.invoke(e)
                }

                override fun mouseDragged(e: MouseEvent) {
                    if (isDragging) {
                        val start = pressPoint ?: return
                        val dx = e.x - start.x
                        val dy = e.y - start.y

                        if (!didDrag &&
                            (
                                abs(dx) > BonsaiTheme.Canvas.PAN_DRAG_THRESHOLD ||
                                    abs(dy) > BonsaiTheme.Canvas.PAN_DRAG_THRESHOLD
                            )
                        ) {
                            didDrag = true
                        }

                        if (didDrag) {
                            onDrag(dx, dy)
                            pressPoint = e.point
                        }
                    }

                    onMove?.invoke(e)
                }
            }

        component.addMouseListener(mouseAdapter)
        component.addMouseMotionListener(mouseAdapter)
        component.addMouseWheelListener { e ->
            when {
                e.isControlDown -> {
                    onMouseWheel(e)
                }

                e.isShiftDown -> {
                    onDrag(-(e.preciseWheelRotation * BonsaiTheme.Canvas.PAN_TRACKPAD_SENSITIVITY).toInt(), 0)
                }

                else -> {
                    if (isDragging) {
                        onDrag(0, -(e.preciseWheelRotation * BonsaiTheme.Canvas.PAN_TRACKPAD_SENSITIVITY).toInt())
                    } else {
                        onMouseWheel(e)
                    }
                }
            }
        }
    }
}
