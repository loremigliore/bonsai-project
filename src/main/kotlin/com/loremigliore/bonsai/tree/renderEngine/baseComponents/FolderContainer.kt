package com.loremigliore.bonsai.tree.renderEngine.baseComponents

import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.Component
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Constraints
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout.Size
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.BasicStroke
import java.awt.Graphics2D

class FolderContainer(
    private val item: TreeItem,
    private val hideFolders: Boolean,
    private val resolveComponent: (Int) -> Component?,
) : Component() {
    override fun layout(
        constraints: Constraints,
        offsetX: Int,
        offsetY: Int,
    ): Size {
        bounds.x = offsetX
        bounds.y = offsetY
        return Size(0, 0)
    }

    override fun draw(graphics: Graphics2D) {
        if (hideFolders) return

        val subtreeComponent = resolveComponent(item.id) ?: return
        val subtreeBounds = subtreeComponent.bounds

        graphics.color = BonsaiTheme.Colors.folderBorder(item.depth)
        graphics.stroke = BasicStroke(BonsaiTheme.Dimensions.treeBorderWidth)
        graphics.drawRoundRect(
            subtreeBounds.x,
            subtreeBounds.y,
            subtreeBounds.width,
            subtreeBounds.height,
            BonsaiTheme.Dimensions.treeArcRadius * 2,
            BonsaiTheme.Dimensions.treeArcRadius * 2,
        )
    }
}
