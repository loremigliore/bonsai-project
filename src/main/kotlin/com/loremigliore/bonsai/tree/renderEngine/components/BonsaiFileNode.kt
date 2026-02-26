package com.loremigliore.bonsai.tree.renderEngine.components

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.logic.utils.searchAction
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.Box
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry
import com.loremigliore.bonsai.tree.renderEngine.baseComponents.ElementTitle
import com.loremigliore.bonsai.tree.viewmodel.ElementViewModel
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.event.MouseEvent
import javax.swing.UIManager

class BonsaiFileNode(
    private val item: TreeItem,
    private val state: CanvasState,
    private val project: Project,
    registry: ComponentRegistry,
    private val onItemClicked: (TreeItem, MouseEvent) -> Unit,
) : Box() {
    init {
        build()
        registry.register(item.id, this)
    }

    private fun build() {
        if (item.depth > state.depth) return

        val viewModel = ElementViewModel(item, state)

        if (viewModel.matchesQuery) {
            project.searchAction.addMatch(item.id)
        }

        val title =
            ElementTitle(
                viewModel = viewModel,
                font = BonsaiTheme.Typography.fileFont,
                colorProvider = { vm ->
                    if (vm.isHovered.value) {
                        BonsaiTheme.Colors.folderText(vm.depth)
                    } else {
                        UIManager.getColor("Label.foreground")
                    }
                },
            ).apply {
                this.onHoverEnter = { viewModel.onHoverEnter() }
                this.onHoverExit = { viewModel.onHoverExit() }
                this.onClick = { e -> onItemClicked(item, e) }
            }

        addChild(title)
    }
}
