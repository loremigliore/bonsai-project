package com.loremigliore.bonsai.domain.states

import com.loremigliore.bonsai.domain.models.VisualizerPermutation
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.ui.components.states.ElementState

data class CanvasState(
    val baseCache: List<TreeItem> = emptyList(),
    val depth: Int = 1,
    val focusedSubtree: List<TreeItem> = emptyList(),
    val focusedDepthOffset: Int = 0,
    val elementStates: Map<Int, Set<ElementState>> = emptyMap(),
    val permutation: VisualizerPermutation = VisualizerPermutation.default(),
    val searchQuery: String? = null,
) {
    val cache: List<TreeItem> get() = focusedSubtree.ifEmpty { baseCache }
}
