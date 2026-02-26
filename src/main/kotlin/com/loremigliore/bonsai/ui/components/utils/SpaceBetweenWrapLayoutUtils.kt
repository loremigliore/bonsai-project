package com.loremigliore.bonsai.ui.components.utils

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager2

class SpaceBetweenWrapLayoutUtils(
    private val horizontalGap: Int = 8,
    private val verticalGap: Int = 8,
) : LayoutManager2 {
    private val positions = mutableMapOf<Component, Float>()

    override fun addLayoutComponent(
        comp: Component,
        constraints: Any?,
    ) {
        if (constraints is Float) positions[comp] = constraints
    }

    override fun addLayoutComponent(
        name: String?,
        comp: Component?,
    ) {
    }

    override fun removeLayoutComponent(comp: Component?) {
        comp?.let { positions.remove(it) }
    }

    override fun maximumLayoutSize(target: Container): Dimension = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

    override fun getLayoutAlignmentX(target: Container) = 0.5f

    override fun getLayoutAlignmentY(target: Container) = 0.5f

    override fun invalidateLayout(target: Container) {}

    override fun minimumLayoutSize(parent: Container): Dimension = preferredLayoutSize(parent)

    override fun preferredLayoutSize(parent: Container): Dimension {
        val insets = parent.insets
        val availableWidth =
            if (parent.width > 0) {
                parent.width - insets.left - insets.right
            } else {
                parent.visibleChildren().sumOf { it.preferredSize.width + horizontalGap }
            }

        val visibleChildren = parent.visibleChildren()
        if (visibleChildren.isEmpty()) return Dimension(0, 0)

        var currentRowWidth = 0
        var currentRowHeight = 0
        var totalHeight = 0

        for (child in visibleChildren) {
            val childSize = child.preferredSize
            if (currentRowWidth > 0 && currentRowWidth + childSize.width > availableWidth) {
                totalHeight += currentRowHeight + verticalGap
                currentRowWidth = 0
                currentRowHeight = 0
            }
            currentRowWidth += childSize.width + horizontalGap
            currentRowHeight = maxOf(currentRowHeight, childSize.height)
        }

        totalHeight += currentRowHeight

        return Dimension(
            availableWidth + insets.left + insets.right,
            totalHeight + insets.top + insets.bottom,
        )
    }

    override fun layoutContainer(parent: Container) {
        val insets = parent.insets
        val availableWidth = usableWidth(parent)
        val visibleChildren = parent.visibleChildren()
        if (visibleChildren.isEmpty()) return

        val totalW = visibleChildren.sumOf { it.preferredSize.width } + horizontalGap * (visibleChildren.size - 1).coerceAtLeast(0)

        if (totalW > availableWidth) {
            var y = insets.top
            visibleChildren.forEach { child ->
                val s = child.preferredSize
                val x = insets.left + (availableWidth - s.width) / 2
                child.setBounds(x, y, s.width, s.height)
                y += s.height + verticalGap
            }
            return
        }

        visibleChildren.forEach { child ->
            val position = positions[child] ?: 0f
            val s = child.preferredSize
            val x = insets.left + ((availableWidth - s.width) * position).toInt()
            child.setBounds(x, insets.top, s.width, s.height)
        }
    }

    private fun usableWidth(parent: Container): Int {
        val insets = parent.insets
        return parent.width - insets.left - insets.right
    }

    private fun Container.visibleChildren(): List<Component> = components.filter { it.isVisible }
}
