package com.loremigliore.bonsai.tree.renderEngine.backbone.core.composition

class State<T>(
    initialValue: T,
) {
    private val listeners = mutableListOf<() -> Unit>()

    var value: T = initialValue
        set(newValue) {
            if (field != newValue) {
                field = newValue
                listeners.forEach { it() }
            }
        }

    fun observe(listener: () -> Unit) {
        listeners.add(listener)
    }
}

fun <T> stateOf(value: T) = State(value)
