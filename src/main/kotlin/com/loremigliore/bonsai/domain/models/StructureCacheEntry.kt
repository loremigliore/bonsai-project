package com.loremigliore.bonsai.domain.models

import com.loremigliore.bonsai.domain.enums.CacheEntryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

data class StructureCacheEntry(
    val status: MutableStateFlow<CacheEntryStatus> =
        MutableStateFlow(CacheEntryStatus.EMPTY),
    var cache: JSONObject? = null,
)
