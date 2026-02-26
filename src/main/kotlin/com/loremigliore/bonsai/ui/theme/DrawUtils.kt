package com.loremigliore.bonsai.ui.theme

import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.border.AbstractBorder

object DrawUtils {
    class CustomRoundedLineBorder(
        private val color: Color = BonsaiTheme.Colors.border,
        private val thickness: Int = 1,
    ) : AbstractBorder() {
        override fun paintBorder(
            c: Component,
            g: Graphics,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
        ) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,
            )

            g2.color = color
            g2.stroke = BasicStroke(thickness.toFloat())

            g2.drawRoundRect(
                x,
                y,
                width - 1,
                height - 1,
                JBUI.scale(10),
                JBUI.scale(10),
            )

            g2.dispose()
        }

        override fun getBorderInsets(c: Component): Insets = JBUI.insets(thickness, thickness, thickness, thickness)
    }

    class CustomSegmentedButtonItem<T> : JButton
        where T : Enum<T> {
        private val selected: Boolean
        private val onClick: () -> Unit

        constructor(
            icon: Icon,
            selected: Boolean,
            onClick: () -> Unit,
        ) : super(icon) {
            this.selected = selected
            this.onClick = onClick
            initButton()
        }

        constructor(
            name: String,
            selected: Boolean,
            onClick: () -> Unit,
        ) : super(name) {
            this.selected = selected
            this.onClick = onClick
            initButton()
        }

        private fun initButton() {
            isFocusable = false
            isOpaque = false
            isContentAreaFilled = false

            border =
                BorderFactory.createEmptyBorder(
                    BonsaiTheme.Spacing.segmentedButtonPadding.top,
                    BonsaiTheme.Spacing.segmentedButtonPadding.left,
                    BonsaiTheme.Spacing.segmentedButtonPadding.bottom,
                    BonsaiTheme.Spacing.segmentedButtonPadding.right,
                )

            addActionListener {
                onClick()
            }
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,
            )

            val backgroundColor =
                if (selected) {
                    BonsaiTheme.Colors.segmentedButtonSelected
                } else {
                    UIUtil.getPanelBackground()
                }

            g2.color = backgroundColor
            g2.fillRoundRect(
                0,
                0,
                width,
                height,
                BonsaiTheme.Dimensions.segmentedButtonArcRadius,
                BonsaiTheme.Dimensions.segmentedButtonArcRadius,
            )

            if (selected) {
                g2.color = BonsaiTheme.Colors.segmentedButtonSelectedBorder
                g2.stroke = BasicStroke(BonsaiTheme.Dimensions.segmentedButtonBorderWidth)

                g2.drawRoundRect(
                    0,
                    0,
                    width - 1,
                    height - 1,
                    BonsaiTheme.Dimensions.segmentedButtonArcRadius,
                    BonsaiTheme.Dimensions.segmentedButtonArcRadius,
                )
            }

            g2.dispose()
            super.paintComponent(g)
        }
    }

    fun fillRoundedBackground(
        g: Graphics,
        component: Component,
        arc: Int = JBUI.scale(10),
    ) {
        val g2 = g as? Graphics2D ?: g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = component.background
        g2.fillRoundRect(0, 0, component.width, component.height, arc, arc)
        if (g !is Graphics2D) g2.dispose()
    }

    fun JLabel.applyListCellStyle(
        list: JList<*>,
        isSelected: Boolean,
        iconTextGap: Int = 0,
    ) {
        this.iconTextGap = iconTextGap
        border = JBUI.Borders.empty(8, 16)
        isOpaque = isSelected
        background = if (isSelected) UIUtil.getListSelectionBackground(true) else list.background
        foreground = if (isSelected) UIUtil.getListSelectionForeground(true) else list.foreground
    }
}
