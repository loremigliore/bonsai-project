package com.loremigliore.bonsai.logic.services.actions

import com.intellij.openapi.components.Service
import com.loremigliore.bonsai.domain.states.ToolbarState
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service(Service.Level.PROJECT)
class ToolbarAction {
    private val settings = BonsaiSettings.getInstance()

    private val _state =
        MutableStateFlow(ToolbarState(settings.hideFolders, settings.hideFiles, settings.hideMiddlePackages, settings.shrinkMiddlePackages))
    val state: StateFlow<ToolbarState> = _state.asStateFlow()

    fun updateState(update: ToolbarState.() -> ToolbarState) {
        _state.value = _state.value.update()
    }

    fun setFoldersHide(value: Boolean) = updateState { copy(hideFolders = value) }

    fun setFilesHide(value: Boolean) = updateState { copy(hideFiles = value) }

    fun setMiddlePackagesHide(value: Boolean) = updateState { copy(hideMiddlePackages = value) }

    fun setMiddlePackagesShrink(value: Boolean) = updateState { copy(shrinkMiddlePackages = value) }
}
