package com.loremigliore.bonsai.ui.components.prototypes

import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import com.loremigliore.bonsai.ui.theme.DrawUtils
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.Icon
import javax.swing.JButton

class CanvasButton(
    val icon: Icon,
    private val onClick: () -> Unit,
) {
    companion object {
        private const val WIDTH = BonsaiTheme.Dimensions.CANVAS_BUTTON_WIDTH
        private const val HEIGHT = BonsaiTheme.Dimensions.CANVAS_BUTTON_HEIGHT

        private const val ARC_RADIUS = BonsaiTheme.Dimensions.CANVAS_BUTTON_ARC_RADIUS
    }

    val component: JButton =
        object : JButton(icon) {
            private val arc = JBUI.scale(ARC_RADIUS)

            init {
                isContentAreaFilled = false
                isFocusPainted = false
                isOpaque = false
                isRolloverEnabled = true
                margin = JBUI.emptyInsets()
                border = null

                val hoverBorder = DrawUtils.CustomRoundedLineBorder()

                model.addChangeListener {
                    border = if (model.isRollover) hoverBorder else null
                    repaint()
                }

                addActionListener { onClick() }
            }

            override fun paintComponent(g: Graphics) {
                DrawUtils.fillRoundedBackground(g, this, arc)
                super.paintComponent(g)
            }

            override fun getPreferredSize() = Dimension(JBUI.scale(CanvasButton.WIDTH), JBUI.scale(CanvasButton.HEIGHT))

            override fun getMinimumSize() = preferredSize

            override fun getMaximumSize() = Dimension(JBUI.scale(CanvasButton.WIDTH), JBUI.scale(CanvasButton.HEIGHT))
        }
}
