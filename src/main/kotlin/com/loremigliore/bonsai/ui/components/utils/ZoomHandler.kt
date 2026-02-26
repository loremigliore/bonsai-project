package com.loremigliore.bonsai.ui.components.utils

import com.loremigliore.bonsai.ui.components.states.ZoomState
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.awt.event.MouseWheelEvent
import kotlin.math.abs

class ZoomHandler {
    private val _zoomState = MutableStateFlow(ZoomState())
    val zoomState: StateFlow<ZoomState> = _zoomState

    data class RecenterEvent(
        val fitZoom: Boolean,
        val resetVertical: Boolean,
    )

    private val recenterCanvas =
        MutableSharedFlow<RecenterEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val fitToContentEvent: Flow<RecenterEvent> = recenterCanvas

    private var fittedZoom: ZoomState = ZoomState()

    fun onMouseWheelMoved(e: MouseWheelEvent) {
        val zoomIn = BonsaiTheme.Canvas.ZOOM_IN_FACTOR
        val zoomOut = BonsaiTheme.Canvas.ZOOM_OUT_FACTOR
        val zoomFactor = if (e.wheelRotation < 0) zoomIn else zoomOut
        _zoomState.update { state ->
            val newScale =
                (state.scale * zoomFactor).coerceIn(
                    BonsaiTheme.Canvas.ZOOM_MIN_SCALE,
                    BonsaiTheme.Canvas.ZOOM_MAX_SCALE,
                )
            if (newScale == state.scale) return@update state

            val mousePoint = e.point
            val worldX = (mousePoint.x / state.scale) - state.offsetX
            val worldY = (mousePoint.y / state.scale) - state.offsetY

            val newOffsetX = (mousePoint.x / newScale) - worldX
            val newOffsetY = (mousePoint.y / newScale) - worldY

            state.copy(
                scale = newScale,
                offsetX = newOffsetX,
                offsetY = newOffsetY,
            )
        }
    }

    fun onDrag(
        dx: Int,
        dy: Int,
    ) {
        _zoomState.update {
            it.copy(
                offsetX = it.offsetX + dx / it.scale,
                offsetY = it.offsetY + dy / it.scale,
            )
        }
    }

    fun setOffset(
        offsetX: Float,
        offsetY: Float,
    ) {
        _zoomState.update { it.copy(offsetX = offsetX, offsetY = offsetY) }
    }

    fun fitToContent(
        canvasWidth: Int,
        canvasHeight: Int,
        contentWidth: Int,
        contentHeight: Int,
        fitZoom: Boolean,
        resetVertical: Boolean,
    ) {
        if (contentWidth <= 0 || contentHeight <= 0) return
        if (canvasWidth <= 0 || canvasHeight <= 0) return

        val padding = BonsaiTheme.Canvas.ZOOM_PADDING.toFloat()
        val availableWidth = canvasWidth - padding * 2
        val currentScale = _zoomState.value.scale

        val newScale =
            if (fitZoom || contentWidth * currentScale > availableWidth) {
                (availableWidth / contentWidth.toFloat()).coerceIn(
                    BonsaiTheme.Canvas.ZOOM_MIN_SCALE,
                    BonsaiTheme.Canvas.ZOOM_MAX_SCALE,
                )
            } else {
                currentScale
            }

        val newOffsetX =
            if (contentWidth * newScale <= availableWidth) {
                (canvasWidth / newScale - contentWidth) / 2f
            } else {
                padding / newScale
            }

        val currentOffsetY = _zoomState.value.offsetY
        val isVisibleVertically =
            (currentOffsetY + contentHeight) * newScale > 0 &&
                currentOffsetY * newScale < canvasHeight

        val newOffsetY =
            if (fitZoom || resetVertical || !isVisibleVertically) {
                padding / newScale
            } else {
                currentOffsetY
            }

        _zoomState.update {
            it.copy(scale = newScale, offsetX = newOffsetX, offsetY = newOffsetY)
        }
        fittedZoom = _zoomState.value
    }

    val isPanned: Flow<Boolean> =
        _zoomState
            .map { current ->
                val dx = abs(current.offsetX - fittedZoom.offsetX)
                val dy = abs(current.offsetY - fittedZoom.offsetY)
                val ds = abs(current.scale - fittedZoom.scale)
                dx > BonsaiTheme.Canvas.PAN_OFFSET_THRESHOLD ||
                    dy > BonsaiTheme.Canvas.PAN_OFFSET_THRESHOLD ||
                    ds > BonsaiTheme.Canvas.PAN_SCALE_THRESHOLD
            }.distinctUntilChanged()

    fun recenter(
        fitZoom: Boolean,
        resetVertical: Boolean,
    ) {
        recenterCanvas.tryEmit(RecenterEvent(fitZoom, resetVertical))
    }
}
