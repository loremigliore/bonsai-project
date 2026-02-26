package com.loremigliore.bonsai.tree.renderEngine.components

import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.Column
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.ContainerComponent
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.Insets
import java.awt.event.MouseEvent

class BonsaiCascadeFolderNode(
    item: TreeItem,
    state: CanvasState,
    registry: ComponentRegistry,
    onItemClicked: (TreeItem, MouseEvent) -> Unit,
    project: Project,
    skippedLevels: Int = 0,
) : BonsaiFolderNode(item, state, TreeLayout.CASCADE, registry, onItemClicked, project) {
    init {
        if (skippedLevels > 0) {
            val baseMargin = BonsaiTheme.Spacing.cascadeContainerMargin
            this.margin = JBUI.insetsLeft(skippedLevels * baseMargin.left)
        }

        buildCommon()
        registerSelf()
    }

    override fun getTitleMargin(): Insets {
        val m = BonsaiTheme.Spacing.cascadeTitleMargin
        return JBUI.insets(m.top, m.left, m.bottom, m.right)
    }

    override fun createChildrenContainer(): ContainerComponent =
        Column(BonsaiTheme.Layout.cascadeVerticalSpacing).apply {
            val m = BonsaiTheme.Spacing.cascadeContainerMargin
            margin = JBUI.insets(m.top, m.left, m.bottom, m.right)
        }

    override fun buildFolderNode(
        child: TreeItem,
        skipped: Int,
    ): BonsaiFolderNode = BonsaiCascadeFolderNode(child, state, registry, onItemClicked, project, skipped)

    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY

        val contentX = offsetX + padding.left
        var cursorY = offsetY + padding.top
        var maxWidth = 0

        for ((index, child) in children.withIndex()) {
            cursorY += child.margin.top
            child.layoutIfNeeded(constraints, contentX + child.margin.left, cursorY)
            cursorY += child.bounds.height + child.margin.bottom
            if (index != children.lastIndex) cursorY += 0
            maxWidth = maxOf(maxWidth, child.bounds.width + child.margin.left + child.margin.right)
        }

        bounds.width = maxWidth + padding.left + padding.right
        bounds.height = (cursorY - offsetY) + padding.bottom

        return Size(bounds.width, bounds.height)
    }

    override fun draw(graphics: java.awt.Graphics2D) {
        drawBackgroundAndBorder(graphics)
        drawChildren(graphics)
    }
}
