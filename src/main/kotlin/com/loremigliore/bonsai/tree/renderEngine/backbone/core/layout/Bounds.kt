package com.loremigliore.bonsai.tree.renderEngine.backbone.core.layout

data class Bounds(
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
) {
    fun contains(
        px: Int,
        py: Int,
    ): Boolean = px in x..(x + width) && py in y..(y + height)
}
