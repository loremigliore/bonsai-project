package com.loremigliore.bonsai.tree.viewmodel

import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition.stateOf
import com.loremigliore.bonsai.ui.components.states.ElementState

class ElementViewModel(
    val item: TreeItem,
    private val state: CanvasState,
    displayNameOverride: String? = null,
) {
    val isHovered = stateOf(false)
    val isSelected = stateOf(state.elementStates[item.id]?.contains(ElementState.SELECTED) == true)
    val displayName = stateOf(displayNameOverride ?: item.name)
    val depth: Int = item.depth

    val matchesQuery: Boolean =
        run {
            val query = state.searchQuery
            if (query.isNullOrBlank()) return@run false
            val name = displayNameOverride ?: item.name
            name.contains(query, ignoreCase = true)
        }

    fun onHoverEnter() {
        isHovered.value = true
    }

    fun onHoverExit() {
        isHovered.value = false
    }

    fun matchRanges(): List<IntRange> {
        val query = state.searchQuery ?: return emptyList()
        if (query.isBlank()) return emptyList()
        val name = displayName.value.lowercase()
        val q = query.lowercase()
        val ranges = mutableListOf<IntRange>()
        var index = 0
        while (index < name.length) {
            val found = name.indexOf(q, index)
            if (found == -1) break
            ranges.add(found until found + q.length)
            index = found + 1
        }
        return ranges
    }
}
