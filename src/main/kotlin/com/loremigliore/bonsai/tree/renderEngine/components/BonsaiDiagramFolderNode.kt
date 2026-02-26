package com.loremigliore.bonsai.tree.renderEngine.components

import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.Row
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.ContainerComponent
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.event.MouseEvent

class BonsaiDiagramFolderNode(
    item: TreeItem,
    state: CanvasState,
    registry: ComponentRegistry,
    onItemClicked: (TreeItem, MouseEvent) -> Unit,
    project: Project,
    skippedLevels: Int = 0,
) : BonsaiFolderNode(item, state, TreeLayout.DIAGRAM, registry, onItemClicked, project) {
    init {
        if (skippedLevels > 0) {
            val baseMargin = BonsaiTheme.Spacing.diagramContainerMargin
            this.margin = JBUI.insetsTop(skippedLevels * baseMargin.top)
        }

        buildCommon()
        registerSelf()
    }

    override fun getTitleMargin(): Insets {
        val m = BonsaiTheme.Spacing.diagramTitleMargin
        return JBUI.insets(m.top, m.left, m.bottom, m.right)
    }

    override fun createChildrenContainer(): ContainerComponent =
        Row(BonsaiTheme.Layout.diagramHorizontalSpacing).apply {
            val m = BonsaiTheme.Spacing.diagramContainerMargin
            margin = JBUI.insets(m.top, m.left, m.bottom, m.right)
        }

    override fun buildFolderNode(
        child: TreeItem,
        skipped: Int,
    ): BonsaiFolderNode = BonsaiDiagramFolderNode(child, state, registry, onItemClicked, project, skipped)

    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY

        var cursorY = offsetY + padding.top
        var maxWidth = 0

        for (child in children) {
            cursorY += child.margin.top
            child.layoutIfNeeded(constraints, offsetX + padding.left + child.margin.left, cursorY)
            cursorY += child.bounds.height + child.margin.bottom
            maxWidth = maxOf(maxWidth, child.bounds.width + child.margin.left + child.margin.right)
        }

        bounds.width = maxWidth + padding.left + padding.right
        bounds.height = (cursorY - offsetY) + padding.bottom

        for (child in children) {
            child.bounds.x = offsetX + (bounds.width - child.bounds.width) / 2
        }

        return Size(bounds.width, bounds.height)
    }

    override fun draw(graphics: Graphics2D) {
        drawBackgroundAndBorder(graphics)
        drawChildren(graphics)
    }
}
