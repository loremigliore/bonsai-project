package com.loremigliore.bonsai.domain.states

import com.loremigliore.bonsai.domain.models.VisualizerPermutation

data class VisualizerState(
    val toolbarState: ToolbarState,
    val headerState: HeaderState,
) {
    fun toVisualizerPermutation(): VisualizerPermutation =
        VisualizerPermutation(
            hideFolders = this.toolbarState.hideFolders,
            hideFiles = this.toolbarState.hideFiles,
            hideMiddlePackages = this.toolbarState.hideMiddlePackages,
            shrinkMiddlePackages = this.toolbarState.shrinkMiddlePackages,
            treeDetail = this.headerState.treeDetail,
            treeLayout = this.headerState.treeLayout,
        )
}
