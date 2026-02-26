package com.loremigliore.bonsai.domain.states

data class SearchState(
    val query: String? = null,
    val matchedIds: List<Int> = emptyList(),
    val currentIndex: Int = 0,
) {
    val currentMatchId: Int? get() = matchedIds.getOrNull(currentIndex)
    val hasMatches: Boolean get() = matchedIds.isNotEmpty()
    val totalMatches: Int get() = matchedIds.size
}
