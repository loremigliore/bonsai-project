package com.loremigliore.bonsai.tree.utils

import com.loremigliore.bonsai.tree.models.TreeItem

object MiddlePackageCollapser {
    fun chainEnd(
        item: TreeItem,
        maxDepth: Int,
    ): TreeItem {
        var current = item
        while (current.isMiddlePackage && current.depth < maxDepth) {
            current = current.children.first()
        }
        return current
    }

    fun chainLabel(
        item: TreeItem,
        maxDepth: Int,
        hideMiddle: Boolean,
    ): String {
        val parts = mutableListOf<String>()
        var current = item
        while (true) {
            val isLast = !current.isMiddlePackage || current.depth >= maxDepth
            parts.add(if (hideMiddle && !isLast) "·" else current.name)
            if (isLast) break
            current = current.children.first()
        }
        return parts.joinToString("/")
    }
}
