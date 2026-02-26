package com.loremigliore.bonsai.logic.services.actions

import com.intellij.openapi.components.Service
import com.loremigliore.bonsai.domain.states.SearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Service(Service.Level.PROJECT)
class SearchAction {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun addMatch(id: Int) {
        _state.update { it.copy(matchedIds = it.matchedIds + id) }
    }

    fun clearMatches() {
        _state.update { it.copy(matchedIds = emptyList(), currentIndex = 0) }
    }

    fun nextMatch() {
        _state.update { state ->
            if (state.hasMatches) {
                state.copy(currentIndex = (state.currentIndex + 1) % state.totalMatches)
            } else {
                state
            }
        }
    }

    fun setQuery(query: String?) {
        _state.update { it.copy(query = query) }
    }
}
