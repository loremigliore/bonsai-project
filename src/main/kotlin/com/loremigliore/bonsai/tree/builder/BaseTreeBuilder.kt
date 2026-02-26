package com.loremigliore.bonsai.tree.builder

import com.loremigliore.bonsai.tree.models.TreeItem
import org.json.JSONObject

object BaseTreeBuilder {
    private const val UNKNOWN_NAME = "unknown"
    private const val ROOT_DEPTH = 1

    fun buildBaseTree(structure: JSONObject?): List<TreeItem> {
        structure ?: return emptyList()

        val idGenerator = IdGenerator()
        val root = buildTreeItem(structure, ROOT_DEPTH, idGenerator)

        return listOf(root)
    }

    private fun buildTreeItem(
        node: JSONObject,
        depth: Int,
        idGenerator: IdGenerator,
    ): TreeItem {
        val id = idGenerator.next()
        val name = node.optString("name", UNKNOWN_NAME)
        val path = node.optString("path", "undefined")
        val children = extractChildren(node, depth, idGenerator)

        return TreeItem(
            id = id,
            depth = depth,
            name = name,
            path = path,
            children = children,
        )
    }

    private fun extractChildren(
        node: JSONObject,
        currentDepth: Int,
        idGenerator: IdGenerator,
    ): List<TreeItem> {
        val childrenArray = node.optJSONArray("children") ?: return emptyList()

        val childDepth = currentDepth + 1
        return (0 until childrenArray.length())
            .map { buildTreeItem(childrenArray.getJSONObject(it), childDepth, idGenerator) }
    }

    private class IdGenerator {
        private var current = 1

        fun next(): Int = current++
    }
}
