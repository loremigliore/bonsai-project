package com.loremigliore.bonsai.tree.builder

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.logic.utils.projectSettings
import org.json.JSONArray
import org.json.JSONObject

object StructureBuilder {
    private val baseExclusions =
        setOf(
            "test",
            "build",
            "target",
            "out",
            "dist",
            ".gradle",
            ".idea",
            "node_modules",
            ".next",
            ".mvn",
            "bin",
            "classes",
            "generated",
            ".nuxt",
            ".output",
        )

    var maxDepth: Int = 1
        private set

    fun buildStructure(
        project: Project,
        detail: TreeDetail,
    ): JSONObject {
        val projectDir = project.guessProjectDir() ?: return JSONObject()
        val exclusions = exclusionsForMode(project, detail)

        val tree = buildFileTree(projectDir, exclusions)
        maxDepth = calculateMaxDepth(tree)

        return tree
    }

    private fun exclusionsForMode(
        project: Project,
        mode: TreeDetail,
    ): Set<String> =
        when (mode) {
            TreeDetail.SIMPLIFIED -> baseExclusions + project.projectSettings.getProjectExclusions()
            TreeDetail.BASE -> baseExclusions
            TreeDetail.INFRA -> emptySet()
        }

    private fun buildFileTree(
        root: VirtualFile,
        exclusions: Set<String>,
    ): JSONObject {
        val rootNode = root.toJsonNode()
        val stack = ArrayDeque<Pair<VirtualFile, JSONObject>>()
        stack.addLast(root to rootNode)

        while (stack.isNotEmpty()) {
            val (file, node) = stack.removeLast()
            if (!file.isDirectory || !file.isValid) continue

            val children = JSONArray()

            file.children.orEmpty().forEach { child ->
                if (!child.isValid) return@forEach
                if (child.name.startsWith(".")) return@forEach
                if (child.name.lowercase() in exclusions) return@forEach

                val childNode = child.toJsonNode()
                children.put(childNode)

                if (child.isDirectory) {
                    stack.addLast(child to childNode)
                }
            }

            if (children.length() > 0) {
                node.put("children", children)
            }
        }

        return rootNode
    }

    private fun calculateMaxDepth(
        json: JSONObject,
        depth: Int = 1,
    ): Int {
        if (!json.optBoolean("isDirectory") || !json.has("children")) return depth

        val dirs =
            json
                .getJSONArray("children")
                .toJsonObjectList()
                .filter { it.optBoolean("isDirectory") }

        return if (dirs.isEmpty()) {
            depth
        } else {
            dirs.maxOf { calculateMaxDepth(it, depth + 1) }
        }
    }

    private fun VirtualFile.toJsonNode(): JSONObject =
        JSONObject().apply {
            put("name", name)
            put("path", path)
            put("isDirectory", isDirectory)
        }

    private fun JSONArray.toJsonObjectList(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
}
