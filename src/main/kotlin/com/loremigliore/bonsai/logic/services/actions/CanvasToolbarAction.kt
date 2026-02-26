package com.loremigliore.bonsai.logic.services.actions

import com.intellij.openapi.components.Service
import com.loremigliore.bonsai.domain.states.CanvasToolbarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service(Service.Level.PROJECT)
class CanvasToolbarAction {
    private val _state = MutableStateFlow(CanvasToolbarState())
    val state: StateFlow<CanvasToolbarState> = _state.asStateFlow()

    fun updateState(update: CanvasToolbarState.() -> CanvasToolbarState) {
        _state.value = _state.value.update()
    }

    fun setShowReset(value: Boolean) = updateState { copy(showReset = value) }

    fun setShowRecenter(value: Boolean) = updateState { copy(showRecenter = value) }

    fun setShowSearchbar(value: Boolean) = updateState { copy(showSearchbar = value) }
}
