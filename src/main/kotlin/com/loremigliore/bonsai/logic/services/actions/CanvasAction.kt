package com.loremigliore.bonsai.logic.services.actions

import com.intellij.openapi.components.Service
import com.loremigliore.bonsai.domain.states.CanvasState
import com.loremigliore.bonsai.tree.models.flattenAll
import com.loremigliore.bonsai.tree.models.normalizeDepth
import com.loremigliore.bonsai.ui.components.states.ZoomState
import com.loremigliore.bonsai.ui.components.utils.ZoomHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.event.MouseWheelEvent

@Service(Service.Level.PROJECT)
class CanvasAction {
    private val _state = MutableStateFlow(CanvasState())
    val state: StateFlow<CanvasState> = _state.asStateFlow()

    private val zoomHandler = ZoomHandler()
    val zoomState: StateFlow<ZoomState> = zoomHandler.zoomState
    val isPanned: Flow<Boolean> = zoomHandler.isPanned

    val fitToContentEvent: Flow<ZoomHandler.RecenterEvent> = zoomHandler.fitToContentEvent

    fun updateState(update: CanvasState.() -> CanvasState) {
        _state.value = _state.value.update()
    }

    fun setFocusedItems(ids: List<Int>?) {
        if (ids.isNullOrEmpty()) {
            updateState { copy(focusedSubtree = emptyList(), focusedDepthOffset = 0) }
            return
        }
        val allItems = state.value.baseCache.flattenAll()
        val matchedItems = ids.mapNotNull { id -> allItems.firstOrNull { it.id == id } }
        if (matchedItems.isEmpty()) return
        val baseCacheRootDepth = state.value.baseCache.minOfOrNull { it.depth } ?: 0
        val minDepth = matchedItems.minOf { it.depth }
        val offset = minDepth - baseCacheRootDepth
        val normalizedItems = matchedItems.map { it.normalizeDepth(offset) }
        val relativeDepth = (state.value.depth - offset).coerceAtLeast(1)
        updateState { copy(focusedSubtree = normalizedItems, focusedDepthOffset = offset, depth = relativeDepth) }
    }

    fun recenter(
        fitZoom: Boolean,
        resetVertical: Boolean,
    ) = zoomHandler.recenter(fitZoom, resetVertical)

    fun onDrag(
        dx: Int,
        dy: Int,
    ) = zoomHandler.onDrag(dx, dy)

    fun onMouseWheelMoved(e: MouseWheelEvent) = zoomHandler.onMouseWheelMoved(e)

    fun setOffset(
        offsetX: Float,
        offsetY: Float,
    ) = zoomHandler.setOffset(offsetX, offsetY)

    fun fitToContent(
        canvasWidth: Int,
        canvasHeight: Int,
        contentWidth: Int,
        contentHeight: Int,
        fitZoom: Boolean,
        resetVertical: Boolean,
    ) = zoomHandler.fitToContent(canvasWidth, canvasHeight, contentWidth, contentHeight, fitZoom, resetVertical)
}
