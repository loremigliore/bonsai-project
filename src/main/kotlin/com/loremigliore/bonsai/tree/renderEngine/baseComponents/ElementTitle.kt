package com.loremigliore.bonsai.tree.renderEngine.baseComponents

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.Component
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import com.loremigliore.bonsai.tree.viewmodel.ElementViewModel
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment

class ElementTitle(
    private val viewModel: ElementViewModel,
    private val font: Font,
    private val titleBorderColor: Color? = null,
    private val titleBorderRadius: Int = 0,
    private val colorProvider: (ElementViewModel) -> Color,
) : Component() {
    init {
        observes(viewModel.isHovered, viewModel.isSelected, viewModel.displayName)
    }

    private fun measureFont(): FontMetrics {
        val g =
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .defaultConfiguration
                .createCompatibleImage(1, 1)
                .createGraphics()
        g.font = font
        return g.fontMetrics
    }

    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY

        val fm = measureFont()
        val p = BonsaiTheme.Spacing.treeContainerPadding
        val displayName = ellipsizeFront(viewModel.displayName.value)

        bounds.width = fm.stringWidth(displayName) + p.left + p.right
        bounds.height = fm.height + p.top + p.bottom

        return Size(bounds.width, bounds.height)
    }

    override fun draw(graphics: Graphics2D) {
        graphics.font = font
        val fm = graphics.fontMetrics
        val displayName = if (viewModel.item.isFolder) ellipsizeFront(viewModel.displayName.value) else viewModel.displayName.value
        val p = BonsaiTheme.Spacing.treeContainerPadding

        val isHovered = viewModel.isHovered.value
        val isSelected = viewModel.isSelected.value
        val arc = titleBorderRadius * 2

        if (isSelected) {
            graphics.color = BonsaiTheme.Colors.treeItemSelected
            if (titleBorderRadius > 0) {
                graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, arc, arc)
            } else {
                graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
            }
        }

        if (titleBorderColor != null) {
            graphics.stroke = BasicStroke(BonsaiTheme.Dimensions.treeBorderWidth)
            graphics.color = titleBorderColor
            if (titleBorderRadius > 0) {
                graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, arc, arc)
            } else {
                graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height)
            }
        }

        val textX = bounds.x + p.left
        val baseline = bounds.y + p.top + fm.ascent
        val ranges = viewModel.matchRanges()

        if (ranges.isEmpty()) {
            graphics.color = colorProvider(viewModel)
            graphics.drawString(displayName, textX, baseline)
        } else {
            var x = textX
            var lastEnd = 0
            ranges.forEach { range ->
                if (range.first > lastEnd) {
                    val prefix = displayName.substring(lastEnd, range.first)
                    graphics.color = colorProvider(viewModel)
                    graphics.drawString(prefix, x, baseline)
                    x += fm.stringWidth(prefix)
                }
                val match = displayName.substring(range.first, range.last + 1)
                val matchW = fm.stringWidth(match)
                graphics.color = BonsaiTheme.Colors.searchHighlightBg
                graphics.fillRoundRect(x, bounds.y + p.top, matchW, fm.height, 4, 4)
                graphics.color = BonsaiTheme.Colors.searchHighlightFg
                graphics.drawString(match, x, baseline)
                x += matchW
                lastEnd = range.last + 1
            }
            if (lastEnd < displayName.length) {
                graphics.color = colorProvider(viewModel)
                graphics.drawString(displayName.substring(lastEnd), x, baseline)
            }
        }

        if (isHovered) {
            graphics.drawLine(textX, baseline + 2, textX + fm.stringWidth(displayName), baseline + 2)
        }
    }

    private fun ellipsizeFront(
        text: String,
        maxChars: Int = 32,
    ): String = if (text.length <= maxChars) text else "..${text.takeLast(maxChars - 2)}"
}
