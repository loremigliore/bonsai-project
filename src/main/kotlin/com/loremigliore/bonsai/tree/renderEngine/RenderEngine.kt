package com.loremigliore.bonsai.tree.renderEngine

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.Column
import com.loremigliore.bonsai.tree.renderEngine.backbone.Row
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry
import com.loremigliore.bonsai.tree.renderEngine.components.BonsaiCascadeFolderNode
import com.loremigliore.bonsai.tree.renderEngine.components.BonsaiDiagramFolderNode
import com.loremigliore.bonsai.tree.renderEngine.components.BonsaiFileNode
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.event.MouseEvent

object RenderEngine {
    fun buildTree(
        state: CanvasState,
        project: Project,
        onItemClicked: (TreeItem, MouseEvent) -> Unit,
    ): RenderResult {
        val layoutMode = state.permutation.treeLayout
        val registries = mutableListOf<ComponentRegistry>()

        val outerRoot =
            when (layoutMode) {
                TreeLayout.CASCADE -> Row(BonsaiTheme.Layout.cascadeRootSpacing)
                TreeLayout.DIAGRAM -> Column(BonsaiTheme.Layout.diagramRootSpacing)
            }

        state.cache.forEach { item ->
            val registry = ComponentRegistry()
            registries.add(registry)
            val itemState = state.copy(depth = state.depth - 1 + item.depth)

            val node =
                if (item.children.isEmpty()) {
                    BonsaiFileNode(item, itemState, project, registry, onItemClicked)
                } else {
                    when (layoutMode) {
                        TreeLayout.CASCADE -> BonsaiCascadeFolderNode(item, itemState, registry, onItemClicked, project)
                        TreeLayout.DIAGRAM -> BonsaiDiagramFolderNode(item, itemState, registry, onItemClicked, project)
                    }
                }
            outerRoot.addChild(node)
        }

        return RenderResult(outerRoot, registries)
    }
}
