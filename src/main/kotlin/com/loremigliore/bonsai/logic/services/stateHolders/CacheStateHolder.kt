package com.loremigliore.bonsai.logic.services.stateHolders

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.enums.CacheEntryStatus
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.domain.models.BaseTreeCacheEntry
import com.loremigliore.bonsai.domain.models.StructureCacheEntry
import com.loremigliore.bonsai.tree.builder.BaseTreeBuilder
import com.loremigliore.bonsai.tree.builder.StructureBuilder.buildStructure
import com.loremigliore.bonsai.tree.models.TreeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Service(Service.Level.PROJECT)
class CacheStateHolder(
    private val project: Project,
    val cs: CoroutineScope,
) : Disposable {
    private val structureCache =
        TreeDetail.entries.associateWith { StructureCacheEntry() }

    private val baseTreeCache =
        TreeDetail.entries.associateWith { BaseTreeCacheEntry() }

    init {
        structureCache.forEach { (detail, entry) ->
            entry.status
                .filter { it == CacheEntryStatus.TO_REBUILD }
                .onEach {
                    entry.status.value = CacheEntryStatus.REBUILDING
                    withContext(Dispatchers.Default) {
                        entry.cache = buildStructure(project = project, detail = detail)
                    }
                    entry.status.value = CacheEntryStatus.BUILT
                }.launchIn(cs)
        }

        structureCache.forEach { (detail, entry) ->
            entry.status
                .filter { it == CacheEntryStatus.BUILT }
                .onEach {
                    baseTreeCache.getValue(detail).status.value = CacheEntryStatus.TO_REBUILD
                }.launchIn(cs)
        }

        baseTreeCache.forEach { (detail, entry) ->
            entry.status
                .filter { it == CacheEntryStatus.TO_REBUILD }
                .onEach {
                    entry.status.value = CacheEntryStatus.REBUILDING
                    withContext(Dispatchers.Default) {
                        entry.cache = BaseTreeBuilder.buildBaseTree(structureCache.getValue(detail).cache)
                    }
                    entry.status.value = CacheEntryStatus.BUILT
                }.launchIn(cs)
        }
    }

    fun structureChanged() {
        structureCache.values.forEach {
            it.status.value = CacheEntryStatus.TO_REBUILD
        }
    }

    fun projectExclusionChanged() {
        structureCache
            .getValue(TreeDetail.SIMPLIFIED)
            .status.value = CacheEntryStatus.TO_REBUILD
    }

    fun observeBaseCacheStatus(detail: TreeDetail): StateFlow<CacheEntryStatus> = baseTreeCache.getValue(detail).status.asStateFlow()

    fun getStructureCache(detail: TreeDetail): JSONObject? = structureCache.getValue(detail).cache

    fun getBaseTreeCache(detail: TreeDetail): List<TreeItem> = baseTreeCache.getValue(detail).cache

    override fun dispose() {}
}
