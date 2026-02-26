package com.loremigliore.bonsai.tree.viewmodel

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.models.VisualizerPermutation
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.logic.utils.projectSettings
import com.loremigliore.bonsai.logic.utils.searchAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.tree.models.ElementAction
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.ui.components.ItemDropdown
import com.loremigliore.bonsai.ui.components.states.ElementState
import com.loremigliore.bonsai.ui.components.states.ZoomState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.awt.Point
import java.awt.event.MouseEvent

@OptIn(FlowPreview::class)
class TreeCanvasViewModel(
    private val project: Project,
) {
    private val elementStates = MutableStateFlow<Map<Int, Set<ElementState>>>(emptyMap())

    val canvasState: StateFlow<CanvasState> =
        project.canvasAction.state
            .combine(elementStates) { state, elementStates ->
                state.copy(elementStates = elementStates)
            }.stateIn(project.visualizerState.cs, SharingStarted.Eagerly, CanvasState())

    val renderState: Flow<CanvasState> =
        canvasState
            .combine(project.searchAction.state) { canvas, search ->
                canvas.copy(searchQuery = search.query)
            }.distinctUntilChanged { old, new ->
                old.cache == new.cache &&
                    old.depth == new.depth &&
                    old.permutation == new.permutation &&
                    old.elementStates == new.elementStates &&
                    old.focusedSubtree == new.focusedSubtree &&
                    old.searchQuery == new.searchQuery
            }
    val zoomState: StateFlow<ZoomState> = project.canvasAction.zoomState
    val isPanned: Flow<Boolean> = project.canvasAction.isPanned

    fun setVisualization(
        cache: List<TreeItem>,
        permutation: VisualizerPermutation,
        depth: Int,
    ) {
        project.canvasAction.updateState {
            copy(
                baseCache = cache,
                permutation = permutation,
                depth = depth,
            )
        }
        elementStates.value = emptyMap()
        project.canvasAction.recenter(fitZoom = false, resetVertical = false)
    }

    fun onItemClicked(
        item: TreeItem,
        e: MouseEvent,
    ) {
        val isModified =
            e.isAltDown || e.isShiftDown ||
                (e.modifiersEx and MouseEvent.CTRL_DOWN_MASK) != 0 ||
                (e.modifiersEx and MouseEvent.META_DOWN_MASK) != 0
        when (e.button) {
            MouseEvent.BUTTON1 if isModified -> onItemAction(item, ElementAction.CTRL_ALT_PRIMARY_CLICK, e)
            MouseEvent.BUTTON1 -> onItemAction(item, ElementAction.PRIMARY_CLICK, e)
            MouseEvent.BUTTON3 -> onItemAction(item, ElementAction.SECONDARY_CLICK, e)
        }
    }

    fun onEmptyClicked(e: MouseEvent) {
        if (e.button == MouseEvent.BUTTON1) {
            elementStates.value = emptyMap()
        }
        if (e.button == MouseEvent.BUTTON3) {
            ItemDropdown.show(
                component = e.component,
                screenPoint = Point(e.x, e.y),
                context =
                    ItemDropdown.Context(
                        isFocused =
                            project.canvasAction.state.value.focusedSubtree
                                .isNotEmpty(),
                        isSelected = false,
                        isAnySelected = elementStates.value.any { (_, states) -> ElementState.SELECTED in states },
                        isEmptyCanvas = true,
                    ),
                onFocus = { focusItems(null) },
                onUnfocus = { unfocus() },
                removeFromSelection = {
                    elementStates.value = emptyMap()
                },
            )
        }
    }

    fun unfocus() {
        val offset = project.canvasAction.state.value.focusedDepthOffset
        val currentDepth = project.canvasAction.state.value.depth
        val restoredDepth = offset + currentDepth
        project.canvasAction.setFocusedItems(null)
        project.headerAction.setCurrentDepth(restoredDepth)
        project.canvasAction.updateState { copy(depth = restoredDepth) }
    }

    private fun onItemAction(
        item: TreeItem,
        action: ElementAction,
        e: MouseEvent,
    ) {
        when (action) {
            ElementAction.PRIMARY_CLICK -> {
                if (item.isFolder) {
                    ElementClickHandler.selectInProjectView(item.path, project)
                } else {
                    ElementClickHandler.openFileInEditor(item.path, project)
                }
            }

            ElementAction.CTRL_ALT_PRIMARY_CLICK -> {
                elementStates.update { current ->
                    val newStates = current.toMutableMap()
                    val states = newStates[item.id]?.toMutableSet() ?: mutableSetOf()

                    if (ElementState.SELECTED in states) {
                        states.remove(ElementState.SELECTED)
                    } else {
                        states.add(ElementState.SELECTED)
                    }

                    if (states.isEmpty()) newStates.remove(item.id) else newStates[item.id] = states
                    newStates
                }
            }

            ElementAction.SECONDARY_CLICK -> {
                ItemDropdown.show(
                    component = e.component,
                    screenPoint = Point(e.x, e.y),
                    context =
                        ItemDropdown.Context(
                            isFocused =
                                project.canvasAction.state.value.focusedSubtree
                                    .isNotEmpty(),
                            isSelected = elementStates.value[item.id]?.contains(ElementState.SELECTED) == true,
                            isAnySelected = elementStates.value.any { (_, states) -> ElementState.SELECTED in states },
                            isEmptyCanvas = false,
                        ),
                    onFocus = {
                        focusItems(item)
                    },
                    onUnfocus = { unfocus() },
                    addToSelection = { addToSelection(item) },
                    removeFromSelection = { removeFromSelection(item) },
                    onExclude = { project.projectSettings.addItemToExclusionListFromCanvas(item.name) },
                )
            }
        }
    }

    fun focusItems(item: TreeItem?) {
        val focusIds =
            if (item != null && elementStates.value[item.id]?.contains(ElementState.SELECTED) != true) {
                listOf(item.id)
            } else {
                elementStates.value
                    .filterValues { ElementState.SELECTED in it }
                    .keys
                    .toList()
            }
        project.canvasAction.setFocusedItems(focusIds)
    }

    fun isAnySelected(): Boolean = elementStates.value.any { (_, states) -> ElementState.SELECTED in states }

    private fun addToSelection(item: TreeItem) {
        elementStates.update { current ->
            val newStates = current.toMutableMap()
            val states = newStates[item.id]?.toMutableSet() ?: mutableSetOf()
            states.add(ElementState.SELECTED)
            newStates[item.id] = states
            newStates
        }
    }

    private fun removeFromSelection(item: TreeItem) {
        elementStates.update { current ->
            val newStates = current.toMutableMap()
            val states = newStates[item.id]?.toMutableSet() ?: return@update current
            states.remove(ElementState.SELECTED)
            if (states.isEmpty()) newStates.remove(item.id) else newStates[item.id] = states
            newStates
        }
    }
}
