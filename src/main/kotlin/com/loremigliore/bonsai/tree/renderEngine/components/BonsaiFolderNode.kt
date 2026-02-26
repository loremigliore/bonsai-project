package com.loremigliore.bonsai.tree.renderEngine.components

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.logic.utils.searchAction
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.ContainerComponent
import com.loremigliore.bonsai.tree.renderEngine.baseComponents.Connector
import com.loremigliore.bonsai.tree.renderEngine.baseComponents.ElementTitle
import com.loremigliore.bonsai.tree.renderEngine.baseComponents.FolderContainer
import com.loremigliore.bonsai.tree.utils.MiddlePackageCollapser
import com.loremigliore.bonsai.tree.viewmodel.ElementViewModel
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.event.MouseEvent

abstract class BonsaiFolderNode(
    protected val item: TreeItem,
    protected val state: CanvasState,
    protected val layoutMode: TreeLayout,
    protected val registry: ComponentRegistry,
    protected val onItemClicked: (TreeItem, MouseEvent) -> Unit,
    protected val project: Project,
) : ContainerComponent() {
    protected val renderItem: TreeItem =
        if (state.permutation.shrinkMiddlePackages && item.isMiddlePackage) {
            MiddlePackageCollapser.chainEnd(item, state.depth)
        } else {
            item
        }

    protected fun buildCommon() {
        if (item.depth > state.depth) return

        val shrink = state.permutation.shrinkMiddlePackages && item.isMiddlePackage
        val displayName =
            when {
                shrink -> MiddlePackageCollapser.chainLabel(item, state.depth, state.permutation.hideMiddlePackages)
                state.permutation.hideMiddlePackages && item.isMiddlePackage -> "·"
                else -> item.name
            }

        val viewModel = ElementViewModel(renderItem, state, displayNameOverride = displayName)

        if (viewModel.matchesQuery) {
            project.searchAction.addMatch(renderItem.id)
        }

        val title =
            ElementTitle(
                viewModel = viewModel,
                font = BonsaiTheme.Typography.folderFont,
                titleBorderColor = BonsaiTheme.Colors.folderBorder(renderItem.depth),
                titleBorderRadius = BonsaiTheme.Dimensions.treeArcRadius,
                colorProvider = { _ -> BonsaiTheme.Colors.folderText(renderItem.depth) },
            ).apply {
                this.onHoverEnter = { viewModel.onHoverEnter() }
                this.onHoverExit = { viewModel.onHoverExit() }
                this.onClick = { e -> onItemClicked(renderItem, e) }
                this.margin = getTitleMargin()
            }

        registry.register(-renderItem.id, title)
        if (item.id != renderItem.id) registry.register(-item.id, title)

        addChild(title)

        val visibleChildren = renderItem.children.filter { it.depth <= state.depth }
        if (visibleChildren.isEmpty()) return

        val childrenContainer = createChildrenContainer()

        visibleChildren.forEach { child ->
            val node =
                if (child.isFolder) {
                    val childShrink = state.permutation.shrinkMiddlePackages && child.isMiddlePackage
                    val chainEnd = if (childShrink) MiddlePackageCollapser.chainEnd(child, state.depth) else child
                    val skipped = chainEnd.depth - child.depth
                    buildFolderNode(child, skipped)
                } else {
                    if (!state.permutation.hideFiles) {
                        BonsaiFileNode(child, state, project, registry, onItemClicked)
                    } else {
                        return@forEach
                    }
                }
            childrenContainer.addChild(node)
        }

        addChild(
            Connector(
                item = renderItem,
                layoutMode = layoutMode,
                maxDepth = state.depth,
                resolveComponent = { id -> registry.resolve(id) },
                resolveTitle = { id -> registry.resolve(-id) },
            ),
        )

        addChild(childrenContainer)

        if (childrenContainer.children.isNotEmpty()) {
            addChild(
                FolderContainer(
                    item = renderItem,
                    hideFolders = state.permutation.hideFolders,
                    resolveComponent = { id -> registry.resolve(id) },
                ),
            )
        }
    }

    protected fun registerSelf() {
        registry.register(renderItem.id, this)
        if (item.id != renderItem.id) registry.register(item.id, this)
    }

    protected abstract fun getTitleMargin(): java.awt.Insets

    protected abstract fun createChildrenContainer(): ContainerComponent

    protected abstract fun buildFolderNode(
        child: TreeItem,
        skipped: Int,
    ): BonsaiFolderNode
}
