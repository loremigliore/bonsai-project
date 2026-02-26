package com.loremigliore.bonsai.ui.coordinator

import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.domain.enums.CacheEntryStatus
import com.loremigliore.bonsai.domain.models.VisualizerPermutation
import com.loremigliore.bonsai.logic.utils.cacheState
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.logic.utils.headerAction
import com.loremigliore.bonsai.logic.utils.searchAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.tree.models.TreeItem
import com.loremigliore.bonsai.tree.models.calculateMaxDepth
import com.loremigliore.bonsai.tree.viewmodel.TreeCanvasViewModel
import com.loremigliore.bonsai.ui.components.Minimap
import com.loremigliore.bonsai.ui.components.TreeCanvas
import com.loremigliore.bonsai.ui.components.states.ElementState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class VisualizerCoordinator(
    private val project: Project,
    private val viewModel: TreeCanvasViewModel,
    private val treeCanvas: TreeCanvas,
    private val minimap: Minimap,
) {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun start(scope: CoroutineScope) {
        setupCacheBuilder(scope)
        setupDepthObserver(scope)
        setupFocusObserver(scope)
        setupCanvasRender(scope)
        setupZoomRender(scope)
        setupFitToContent(scope)
        setupSearchObserver(scope)
        setupSearchScroll(scope)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun setupCacheBuilder(scope: CoroutineScope) {
        project.visualizerState.state
            .debounce { 50 }
            .distinctUntilChanged()
            .flatMapLatest { state ->
                project.cacheState.observeBaseCacheStatus(state.headerState.treeDetail).map { status ->
                    state to status
                }
            }.onEach { (state, status) ->
                if (status == CacheEntryStatus.BUILT) {
                    val permutation = state.toVisualizerPermutation()
                    val cache = project.cacheState.getBaseTreeCache(state.headerState.treeDetail)
                    val currentDepth = state.headerState.currentDepth
                    viewModel.setVisualization(cache, permutation, currentDepth)

                    val activeCache = project.canvasAction.state.value.cache
                    val maxDepth = activeCache.calculateMaxDepth(permutation.hideFiles)
                    project.headerAction.setMaxDepth(maxDepth)
                }
            }.launchIn(scope)
    }

    private fun setupDepthObserver(scope: CoroutineScope) {
        project.headerAction.state
            .map { it.currentDepth }
            .distinctUntilChanged()
            .onEach { depth ->
                project.canvasAction.updateState { copy(depth = depth) }
            }.launchIn(scope)
    }

    private fun setupFocusObserver(scope: CoroutineScope) {
        project.canvasAction.state
            .map { it.focusedSubtree }
            .distinctUntilChanged()
            .onEach { focusedSubtree ->
                val currentState = project.canvasAction.state.value
                val activeCache = focusedSubtree.ifEmpty { currentState.baseCache }
                val maxDepth = activeCache.calculateMaxDepth(currentState.permutation.hideFiles)
                project.headerAction.setMaxDepth(maxDepth)
                if (focusedSubtree.isNotEmpty()) {
                    project.headerAction.setCurrentDepth(currentState.depth)
                }
            }.launchIn(scope)
    }

    @OptIn(FlowPreview::class)
    private fun setupCanvasRender(scope: CoroutineScope) {
        var previousCache: List<TreeItem> = emptyList()
        var previousDepth = 0
        var previousPermutation: VisualizerPermutation? = null
        var previousElementStates: Map<Int, Set<ElementState>> = emptyMap()
        var previousFocusedSubtree: List<TreeItem> = emptyList()
        var previousQuery: String? = null
        viewModel.renderState
            .debounce(50)
            .onEach { state ->
                val cacheChanged = state.cache != previousCache
                val permutationChanged = state.permutation != previousPermutation
                val depthChanged = state.depth != previousDepth
                val elementStatesChanged = state.elementStates != previousElementStates
                val focusedSubtreeChanged = state.focusedSubtree != previousFocusedSubtree
                val queryChanged = state.searchQuery != previousQuery

                val fitZoom = cacheChanged || permutationChanged
                val isSelectionOnly =
                    elementStatesChanged && !cacheChanged && !permutationChanged && !depthChanged && !focusedSubtreeChanged
                val isQueryOnly = queryChanged && !cacheChanged && !permutationChanged && !depthChanged && !focusedSubtreeChanged
                val shouldRecenter = !isSelectionOnly && !isQueryOnly && (fitZoom || depthChanged || focusedSubtreeChanged)

                previousCache = state.cache
                previousDepth = state.depth
                previousPermutation = state.permutation
                previousElementStates = state.elementStates
                previousFocusedSubtree = state.focusedSubtree
                previousQuery = state.searchQuery

                treeCanvas.renderTree(state)
                minimap.invalidateSnapshot()
                if (shouldRecenter) project.canvasAction.recenter(fitZoom, fitZoom)
            }.launchIn(scope)
    }

    private fun setupFitToContent(scope: CoroutineScope) {
        scope.launch {
            project.canvasAction.fitToContentEvent
                .collectLatest { recenterEvent ->
                    withContext(Dispatchers.Main) {
                        suspendCancellableCoroutine { continuation ->
                            val comp = treeCanvas.composition
                            if (comp == null) {
                                continuation.resume(Unit)
                                return@suspendCancellableCoroutine
                            }

                            fun tryFit(): Boolean {
                                val b = comp.rootComponent.bounds
                                val w = treeCanvas.component.width
                                val h = treeCanvas.component.height
                                return if (b.width > 0 && b.height > 0 && w > 0 && h > 0) {
                                    project.canvasAction.fitToContent(
                                        w,
                                        h,
                                        b.width,
                                        b.height,
                                        recenterEvent.fitZoom,
                                        recenterEvent.resetVertical,
                                    )
                                    true
                                } else {
                                    false
                                }
                            }

                            if (tryFit()) {
                                continuation.resume(Unit)
                                return@suspendCancellableCoroutine
                            }

                            comp.rootComponent.onLayoutComplete = {
                                tryFit()
                                if (continuation.isActive) continuation.resume(Unit)
                            }

                            continuation.invokeOnCancellation {
                                comp.rootComponent.onLayoutComplete = null
                            }
                        }
                    }
                }
        }
    }

    private fun setupZoomRender(scope: CoroutineScope) {
        viewModel.zoomState
            .onEach { treeCanvas.handleZoomChange() }
            .launchIn(scope)
    }

    private fun setupSearchObserver(scope: CoroutineScope) {
        project.searchAction.state
            .map { it.query }
            .distinctUntilChanged()
            .onEach {
                val currentState = viewModel.canvasState.value
                treeCanvas.renderTree(currentState)
            }.launchIn(scope)
    }

    private fun setupSearchScroll(scope: CoroutineScope) {
        project.searchAction.state
            .map { it.currentMatchId }
            .distinctUntilChanged()
            .onEach { id -> if (id != null) treeCanvas.scrollToItem(id) }
            .launchIn(scope)
    }
}
