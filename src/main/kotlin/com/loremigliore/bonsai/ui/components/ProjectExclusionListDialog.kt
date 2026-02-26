package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import com.loremigliore.bonsai.logic.utils.projectSettings
import com.loremigliore.bonsai.ui.components.prototypes.BonsaiDialog
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.DefaultListModel
import javax.swing.JComponent

object ProjectExclusionListDialog {
    fun show(project: Project) {
        val dialog = InternalDialog(project)
        if (dialog.showAndGet()) {
            project.projectSettings.setProjectExclusions(dialog.getExclusions())
        }
    }

    private class InternalDialog(
        private val project: Project,
    ) : BonsaiDialog(BonsaiBundle.message("project.exclusion.list.dialog.title")) {
        private val model = DefaultListModel<String>()
        private val list = JBList(model)

        init {
            project.projectSettings.getProjectExclusions().forEach { model.addElement(it) }
            list.emptyText.text = BonsaiBundle.message("project.exclusion.list.empty.text")
            init()
        }

        override fun createCenterPanel(): JComponent {
            val panel =
                ToolbarDecorator
                    .createDecorator(list)
                    .setAddAction {
                        val value =
                            Messages.showInputDialog(
                                BonsaiBundle.message("project.exclusion.list.add.dialog.message"),
                                BonsaiBundle.message("project.exclusion.list.add.dialog.title"),
                                null,
                            )
                        if (!value.isNullOrBlank()) model.addElement(value)
                    }.setRemoveAction {
                        val index = list.selectedIndex
                        if (index >= 0) model.remove(index)
                    }.setEditAction {
                        val index = list.selectedIndex
                        if (index < 0) return@setEditAction
                        val oldValue = model[index]
                        val newValue =
                            Messages.showInputDialog(
                                BonsaiBundle.message("project.exclusion.list.edit.dialog.message"),
                                BonsaiBundle.message("project.exclusion.list.edit.dialog.title"),
                                null,
                                oldValue,
                                null,
                            )
                        if (!newValue.isNullOrBlank()) model[index] = newValue
                    }.disableUpDownActions()
                    .createPanel()

            panel.preferredSize = Dimension(500, 350)
            return panel
        }

        override fun createActions(): Array<out Action?> = arrayOf(ResetAction(), okAction, cancelAction)

        private inner class ResetAction : DialogWrapperAction(BonsaiBundle.message("project.exclusion.list.reset.action.text")) {
            override fun doAction(e: ActionEvent?) {
                val confirm =
                    Messages.showYesNoDialog(
                        project,
                        BonsaiBundle.message("project.exclusion.list.reset.dialog.message"),
                        BonsaiBundle.message("project.exclusion.list.reset.dialog.title"),
                        null,
                    )
                if (confirm != Messages.YES) return
                model.clear()
                BonsaiSettings.getInstance().defaultExclusions.forEach { model.addElement(it) }
            }
        }

        fun getExclusions(): List<String> = (0 until model.size()).map { model[it] }
    }
}
