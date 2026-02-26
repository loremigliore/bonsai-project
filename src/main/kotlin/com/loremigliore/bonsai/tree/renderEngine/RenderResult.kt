package com.loremigliore.bonsai.tree.renderEngine

import com.loremigliore.bonsai.tree.renderEngine.backbone.core.Component
import com.loremigliore.bonsai.tree.renderEngine.backbone.core.ComponentRegistry

data class RenderResult(
    val rootComponent: Component,
    val registries: List<ComponentRegistry>,
)
