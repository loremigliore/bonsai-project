package com.loremigliore.bonsai.logic.services.actions

import com.intellij.openapi.components.Service
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.domain.states.HeaderState
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service(Service.Level.PROJECT)
class HeaderAction {
    private val settings = BonsaiSettings.getInstance()

    private val _state = MutableStateFlow(HeaderState(1, 1, settings.treeDetail, settings.treeLayout))
    val state: StateFlow<HeaderState> = _state.asStateFlow()

    private fun updateState(update: HeaderState.() -> HeaderState) {
        _state.value = _state.value.update()
    }

    fun setMaxDepth(depth: Int) = updateState { copy(maxDepth = depth) }

    fun setCurrentDepth(depth: Int) = updateState { copy(currentDepth = depth) }

    fun setTreeDetail(value: TreeDetail) = updateState { copy(treeDetail = value) }

    fun setTreeLayout(value: TreeLayout) = updateState { copy(treeLayout = value) }
}
