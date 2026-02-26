package com.loremigliore.bonsai.domain.states

import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.domain.enums.TreeLayout

data class HeaderState(
    val maxDepth: Int,
    val currentDepth: Int,
    val treeDetail: TreeDetail,
    val treeLayout: TreeLayout,
)
