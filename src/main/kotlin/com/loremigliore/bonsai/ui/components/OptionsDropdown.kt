package com.loremigliore.bonsai.ui.components

import com.intellij.icons.AllIcons
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.services.stateHolders.VisualizerStateHolder
import com.loremigliore.bonsai.logic.utils.cacheState
import com.loremigliore.bonsai.logic.utils.canvasAction
import com.loremigliore.bonsai.logic.utils.canvasToolbarAction
import com.loremigliore.bonsai.settings.BonsaiSettingsConfigurable
import com.loremigliore.bonsai.ui.icons.PluginIcons
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import com.loremigliore.bonsai.ui.theme.DrawUtils.applyListCellStyle
import java.awt.Point
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants

class OptionsDropdown(
    private val project: Project,
    private val treeCanvas: TreeCanvas? = null,
) {
    enum class DropdownOption(
        val label: String,
        val icon: Icon,
    ) {
        FOCUS_SELECTED(BonsaiBundle.message("options.dropdown.focus_selected"), PluginIcons.Focus),
        UNFOCUS(BonsaiBundle.message("options.dropdown.unfocus"), PluginIcons.Unfocus),
        SEARCH(BonsaiBundle.message("options.dropdown.search"), PluginIcons.Search),
        COPY_JSON(BonsaiBundle.message("options.dropdown.copy"), PluginIcons.Copy),
        EXPORT_IMAGE(BonsaiBundle.message("options.dropdown.export"), PluginIcons.Export),
        PROJECT_EXCLUSION_LIST(
            BonsaiBundle.message("options.dropdown.project_exclusion_list"),
            PluginIcons.ExclusionList,
        ),
        SETTINGS(BonsaiBundle.message("options.dropdown.settings"), PluginIcons.Settings),
    }

    val component: JButton =
        JButton(BonsaiBundle.message("options.dropdown.button.text"), AllIcons.General.ChevronDown).apply {
            margin = BonsaiTheme.Spacing.dropdownButtonMargin
            border = BonsaiTheme.Components.dropdownBorder

            horizontalTextPosition = SwingConstants.LEFT
            iconTextGap = 8

            addActionListener {
                showPopup()
            }
        }

    private fun showPopup() {
        val state = project.canvasAction.state.value
        val items =
            DropdownOption.entries.filter { option ->
                when (option) {
                    DropdownOption.FOCUS_SELECTED -> treeCanvas?.viewModel?.isAnySelected() == true
                    DropdownOption.UNFOCUS -> state.focusedSubtree.isNotEmpty()
                    else -> true
                }
            }

        val popup: ListPopup =
            JBPopupFactory.getInstance().createListPopup(
                object : BaseListPopupStep<DropdownOption>(null, items) {
                    override fun getTextFor(value: DropdownOption): String = value.label

                    override fun getIconFor(value: DropdownOption): Icon = value.icon

                    override fun onChosen(
                        selectedValue: DropdownOption,
                        finalChoice: Boolean,
                    ): PopupStep<*>? = doFinalStep { handleSelection(selectedValue) }
                },
            )

        val popupImpl = popup as? ListPopupImpl ?: return
        popupImpl.list.cellRenderer =
            ListCellRenderer { list, value, _, isSelected, _ ->
                val item = value as DropdownOption
                JLabel(item.label, item.icon, SwingConstants.LEADING).apply {
                    applyListCellStyle(list, isSelected, iconTextGap = 8)
                }
            }

        val location = RelativePoint(component, Point(0, component.height + JBUI.scale(8)))
        popup.show(location)
    }

    private fun handleSelection(selectedValue: DropdownOption) {
        when (selectedValue) {
            DropdownOption.FOCUS_SELECTED -> {
                treeCanvas?.viewModel?.focusItems(null)
            }

            DropdownOption.UNFOCUS -> {
                treeCanvas?.viewModel?.unfocus()
            }

            DropdownOption.SEARCH -> {
                project.canvasToolbarAction.setShowSearchbar(true)
            }

            DropdownOption.COPY_JSON -> {
                copyCurrentTreeToClipboard()
            }

            DropdownOption.EXPORT_IMAGE -> {
                val image = treeCanvas?.exportAsImage() ?: return
                ImageExportDialog.show(project, image)
            }

            DropdownOption.PROJECT_EXCLUSION_LIST -> {
                ProjectExclusionListDialog.show(
                    project,
                )
            }

            DropdownOption.SETTINGS -> {
                ShowSettingsUtil.getInstance().showSettingsDialog(
                    project,
                    BonsaiSettingsConfigurable::class.java,
                )
            }
        }
    }

    private fun copyCurrentTreeToClipboard() {
        val stateHolder = project.getService(VisualizerStateHolder::class.java)

        val json =
            project.cacheState.getStructureCache(stateHolder.state.value.headerState.treeDetail)

        Toolkit
            .getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(json.toString()), null)
    }
}
