package com.loremigliore.bonsai.settings.components

import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import javax.swing.DefaultListModel
import javax.swing.JPanel

class DefaultExclusionListSection {
    private val model = DefaultListModel<String>()

    private val list =
        JBList(model).apply {
            emptyText.text = BonsaiBundle.message("settings.exclusion.list.empty.text")
        }

    val component: JPanel =
        ToolbarDecorator
            .createDecorator(list)
            .setAddAction {
                val value =
                    Messages.showInputDialog(
                        BonsaiBundle.message("settings.exclusion.list.add.dialog.message"),
                        BonsaiBundle.message("settings.exclusion.list.add.dialog.title"),
                        null,
                    )

                if (!value.isNullOrBlank()) {
                    model.addElement(value)
                }
            }.setRemoveAction {
                val index = list.selectedIndex
                if (index >= 0) {
                    model.remove(index)
                }
            }.setEditAction {
                val index = list.selectedIndex
                if (index < 0) return@setEditAction

                val oldValue = model[index]

                val newValue =
                    Messages.showInputDialog(
                        BonsaiBundle.message("settings.exclusion.list.edit.dialog.message"),
                        BonsaiBundle.message("settings.exclusion.list.edit.dialog.title"),
                        null,
                        oldValue,
                        null,
                    )

                if (!newValue.isNullOrBlank()) {
                    model[index] = newValue
                }
            }.disableUpDownActions()
            .createPanel()

    fun loadFrom(settings: BonsaiSettings) {
        model.clear()
        settings.defaultExclusions.forEach { model.addElement(it) }
    }

    fun applyTo(settings: BonsaiSettings) {
        settings.defaultExclusions = model.elements().toList().toMutableList()
    }

    fun isModified(settings: BonsaiSettings): Boolean = settings.defaultExclusions != model.elements().toList()
}
