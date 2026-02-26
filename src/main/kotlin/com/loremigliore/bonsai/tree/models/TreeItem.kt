package com.loremigliore.bonsai.tree.models

data class TreeItem(
    val id: Int,
    val depth: Int,
    val name: String,
    val path: String,
    val children: List<TreeItem> = emptyList(),
) {
    val isFolder: Boolean
        get() = children.isNotEmpty()

    val isMiddlePackage: Boolean
        get() = depth != 0 && children.size == 1 && children.first().isFolder
}

fun TreeItem.normalizeDepth(offset: Int): TreeItem =
    copy(
        depth = depth - offset,
        children = children.map { it.normalizeDepth(offset) },
    )

fun List<TreeItem>.calculateMaxDepth(hideFiles: Boolean): Int {
    fun TreeItem.maxDepth(): Int {
        if (children.isEmpty()) return if (hideFiles) -1 else depth
        return maxOf(depth, children.maxOf { it.maxDepth() })
    }
    return (maxOfOrNull { it.maxDepth() } ?: 0).coerceAtLeast(1)
}

fun List<TreeItem>.flattenAll(): List<TreeItem> =
    buildList {
        for (item in this@flattenAll) {
            add(item)
            addAll(item.children.flattenAll())
        }
    }
