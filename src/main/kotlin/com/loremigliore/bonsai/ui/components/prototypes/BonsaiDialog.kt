package com.loremigliore.bonsai.ui.components.prototypes

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.Action
import javax.swing.JComponent

abstract class BonsaiDialog(
    private var title: String,
    canBeParent: Boolean = true,
) : DialogWrapper(canBeParent) {
    init {
        this.title = title
    }

    abstract override fun createCenterPanel(): JComponent

    override fun createActions(): Array<out Action?> = arrayOf(okAction, cancelAction)
}
