package com.loremigliore.bonsai.tree.renderEngine.backbone.core

class ComponentRegistry {
    private val map = mutableMapOf<Int, Component>()

    fun register(
        id: Int,
        component: Component,
    ) {
        map[id] = component
    }

    fun resolve(id: Int): Component? = map[id]
}
