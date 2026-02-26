package com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout

data class Constraints(
    val maxWidth: Int = Int.MAX_VALUE,
    val maxHeight: Int = Int.MAX_VALUE,
) {
    companion object {
        val Infinite = Constraints()
    }
}
