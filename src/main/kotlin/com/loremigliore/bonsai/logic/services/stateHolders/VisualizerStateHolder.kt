package com.loremigliore.bonsai.logic.services.stateHolders

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.states.VisualizerState
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.logic.utils.toolbarAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Service(Service.Level.PROJECT)
class VisualizerStateHolder(
    project: Project,
    val cs: CoroutineScope,
) : Disposable {
    val state: StateFlow<VisualizerState> =
        combine(
            project.toolbarAction.state,
            project.headerAction.state,
        ) { toolbar, header ->
            VisualizerState(
                toolbarState = toolbar,
                headerState = header,
            )
        }.stateIn(
            scope = cs,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                VisualizerState(
                    toolbarState = project.toolbarAction.state.value,
                    headerState = project.headerAction.state.value,
                ),
        )

    override fun dispose() {
        cs.cancel()
    }
}
