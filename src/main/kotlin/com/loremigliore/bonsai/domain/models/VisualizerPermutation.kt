package com.loremigliore.bonsai.domain.models

import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.domain.enums.TreeLayout

data class VisualizerPermutation(
    val hideFolders: Boolean,
    val hideFiles: Boolean,
    val hideMiddlePackages: Boolean,
    val shrinkMiddlePackages: Boolean,
    val treeDetail: TreeDetail,
    val treeLayout: TreeLayout,
) {
    companion object {
        fun default() =
            VisualizerPermutation(
                hideFolders = true,
                hideFiles = true,
                hideMiddlePackages = true,
                shrinkMiddlePackages = true,
                treeDetail = TreeDetail.BASE,
                treeLayout = TreeLayout.CASCADE,
            )
    }
}
