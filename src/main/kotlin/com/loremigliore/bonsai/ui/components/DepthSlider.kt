package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.BasicStroke
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.Timer

class DepthSlider(
    val project: Project,
) {
    companion object {
        private const val MIN: Int = 1
        private val preferred = Dimension(BonsaiTheme.Dimensions.DEPTH_SLIDER_PREFERRED_WIDTH, BonsaiTheme.Dimensions.DEPTH_SLIDER_HEIGHT)
        private val minimum = Dimension(BonsaiTheme.Dimensions.DEPTH_SLIDER_MINIMUM_WIDTH, BonsaiTheme.Dimensions.DEPTH_SLIDER_HEIGHT)
        private val maximum = Dimension(BonsaiTheme.Dimensions.DEPTH_SLIDER_MAXIMUM_WIDTH, BonsaiTheme.Dimensions.DEPTH_SLIDER_HEIGHT)
    }

    private val _state = MutableStateFlow(1)
    val state: StateFlow<Int> = _state.asStateFlow()

    var value: Int
        get() = _state.value
        set(newValue) {
            _state.value = newValue.coerceIn(MIN, max)
            project.headerAction.setCurrentDepth(_state.value)
            component.repaint()
        }
    private var labelWidth: Int = BonsaiTheme.Dimensions.SLIDER_LABEL_WIDTH
    private var thumbDiameter: Int = BonsaiTheme.Dimensions.SLIDER_THUMB_DIAMETER

    private val labelColor = BonsaiTheme.Colors.depthSliderLabel
    var max: Int = 1
        set(newMax) {
            if (field != newMax) {
                field = newMax
                value = value.coerceIn(MIN, max)
                triggerTemporaryTicks()
                component.repaint()
            }
        }

    val component: JComponent by lazy {
        SliderComponent().also { slider ->
            installMouseHandlers(slider)
        }
    }

    private var isHoveringThumb = false
    private var isDragging = false
    private var isHovering = false

    private var showTicksTemporarily = false
    private var tickTimer: Timer? = null

    private val trackStartX: Int
        get() = component.insets.left + thumbDiameter / 2

    private val trackEndX: Int
        get() = component.width - component.insets.right - labelWidth - thumbDiameter / 2

    private val trackCenterY: Int
        get() = component.height / 2

    private fun thumbX(): Int {
        if (max == MIN) return trackStartX
        val ratio = (value - MIN).toFloat() / (max - MIN)
        return trackStartX + (ratio * (trackEndX - trackStartX)).toInt()
    }

    private fun isOverThumb(
        x: Int,
        y: Int,
    ): Boolean {
        val dx = x - thumbX()
        val dy = y - trackCenterY
        val r = thumbDiameter / 2
        return dx * dx + dy * dy <= r * r
    }

    private fun updateValueFromX(x: Int) {
        if (max == MIN) return

        val ratio =
            ((x - trackStartX).toFloat() / (trackEndX - trackStartX))
                .coerceIn(0f, 1f)

        value = MIN + (ratio * (max - MIN)).toInt()
    }

    private fun installMouseHandlers(slider: JComponent) {
        val adapter =
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (isOverThumb(e.x, e.y)) {
                        isDragging = true
                    }
                    updateValueFromX(e.x)
                }

                override fun mouseReleased(e: MouseEvent) {
                    isDragging = false
                    slider.repaint()
                }

                override fun mouseMoved(e: MouseEvent) {
                    val hoveringThumb = isOverThumb(e.x, e.y)
                    val hoveringSlider = slider.contains(e.point)

                    if (hoveringThumb != isHoveringThumb || hoveringSlider != isHovering) {
                        isHoveringThumb = hoveringThumb
                        isHovering = hoveringSlider
                        slider.repaint()
                    }
                }

                override fun mouseDragged(e: MouseEvent) {
                    if (isDragging) updateValueFromX(e.x)
                }

                override fun mouseExited(e: MouseEvent) {
                    isHovering = false
                    isHoveringThumb = false
                    slider.repaint()
                }
            }

        slider.addMouseListener(adapter)
        slider.addMouseMotionListener(adapter)

        slider.addMouseWheelListener {
            value -= it.wheelRotation
        }
    }

    private inner class SliderComponent : JComponent() {
        init {
            isFocusable = false
            border = BorderFactory.createEmptyBorder(0, 12, 0, 0)

            preferredSize = preferred
            minimumSize = minimum
            maximumSize = maximum
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)

            val g2 = g as Graphics2D
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,
            )

            val cy = trackCenterY

            g2.color = labelColor
            g2.stroke = BasicStroke(1f)
            g2.drawLine(trackStartX, cy, trackEndX, cy)

            if (isHovering || showTicksTemporarily) {
                drawTicks(g2)
            }

            val tx = thumbX() - thumbDiameter / 2
            val ty = cy - thumbDiameter / 2

            g2.color = labelColor
            g2.stroke = BasicStroke(2f)
            g2.drawOval(tx, ty, thumbDiameter, thumbDiameter)

            val text = value.toString()
            val fm = g2.fontMetrics

            val labelX =
                width - labelWidth + (labelWidth - fm.stringWidth(text)) / 2

            val labelY =
                cy + fm.ascent / 2 - fm.descent / 2

            g2.color = labelColor
            g2.drawString(text, labelX, labelY)
        }

        override fun getPreferredSize(): Dimension = preferred

        override fun getMinimumSize(): Dimension = minimum

        override fun getMaximumSize(): Dimension = maximum

        override fun isMaximumSizeSet(): Boolean = true

        private fun drawTicks(g2: Graphics2D) {
            if (max <= MIN) return

            val tickRadius = 2
            val totalTicks = max - MIN + 1
            val trackWidth = trackEndX - trackStartX
            val spacing = trackWidth.toFloat() / (totalTicks - 1)

            g2.color = UIUtil.getPanelBackground()

            for (i in 0 until totalTicks) {
                val x = (trackStartX + i * spacing).toInt()
                val y = trackCenterY
                g2.fillOval(
                    x - tickRadius,
                    y - tickRadius,
                    tickRadius * 2,
                    tickRadius * 2,
                )
            }
        }
    }

    private fun triggerTemporaryTicks() {
        showTicksTemporarily = true
        tickTimer?.stop()

        tickTimer =
            Timer(1250) {
                showTicksTemporarily = false
                component.repaint()
            }.apply {
                isRepeats = false
                start()
            }
    }
}
