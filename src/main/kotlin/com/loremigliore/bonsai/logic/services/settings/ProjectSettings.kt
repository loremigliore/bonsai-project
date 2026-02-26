package com.loremigliore.bonsai.logic.services.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.logic.utils.cacheState
import com.loremigliore.bonsai.logic.utils.headerAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service(Service.Level.PROJECT)
@State(
    name = "com.loremigliore.bonsai.logic.services.settings.ProjectSettings",
    storages = [Storage("bonsai.xml")],
)
class ProjectSettings(
    val project: Project,
) : PersistentStateComponent<ProjectSettings.State> {
    data class State(
        var projectExclusions: List<String>? = null,
    )

    private var state = State()
    private val _projectExclusionsFlow = MutableStateFlow<List<String>>(emptyList())
    val projectExclusionsFlow: StateFlow<List<String>> = _projectExclusionsFlow.asStateFlow()

    override fun getState(): State = state

    override fun loadState(state: State) {
        val exclusions =
            state.projectExclusions
                ?: BonsaiSettings.getInstance().defaultExclusions.toList()
        this.state = state.copy(projectExclusions = exclusions)
        _projectExclusionsFlow.value = exclusions
    }

    override fun noStateLoaded() {
        val exclusions = BonsaiSettings.getInstance().defaultExclusions.toList()
        state = State(projectExclusions = exclusions)
        _projectExclusionsFlow.value = exclusions
    }

    fun getProjectExclusions(): List<String> = state.projectExclusions.orEmpty()

    fun setProjectExclusions(values: List<String>) {
        state = state.copy(projectExclusions = values)
        _projectExclusionsFlow.value = values
        project.cacheState.projectExclusionChanged()
    }

    fun addItemToExclusionListFromCanvas(name: String) {
        val updated = getProjectExclusions() + name
        state = state.copy(projectExclusions = updated)
        _projectExclusionsFlow.value = updated
        project.cacheState.projectExclusionChanged()
        project.headerAction.setTreeDetail(TreeDetail.SIMPLIFIED)
    }
}
