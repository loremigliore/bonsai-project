package com.loremigliore.bonsai.domain.models

import com.loremigliore.bonsai.domain.enums.CacheEntryStatus
import com.loremigliore.bonsai.tree.models.TreeItem
import kotlinx.coroutines.flow.MutableStateFlow

data class BaseTreeCacheEntry(
    val status: MutableStateFlow<CacheEntryStatus> =
        MutableStateFlow(CacheEntryStatus.EMPTY),
    var cache: List<TreeItem> = emptyList(),
)
