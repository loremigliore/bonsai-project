package com.loremigliore.bonsai.settings.components

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import com.loremigliore.bonsai.logic.utils.requireProject
import com.loremigliore.bonsai.ui.components.ProjectExclusionListDialog
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class BottomSection(
    private val defaultExclusionListSection: DefaultExclusionListSection,
) {
    val resetButton =
        JButton(BonsaiBundle.message("settings.reset.button.text"), AllIcons.General.Reset).apply {

            margin = BonsaiTheme.Spacing.settingsButtonMargin
            isFocusable = false
            horizontalTextPosition = SwingConstants.LEFT

            addActionListener {
                val confirm =
                    Messages.showYesNoDialog(
                        requireProject(),
                        BonsaiBundle.message("settings.reset.dialog.message"),
                        BonsaiBundle.message("settings.reset.dialog.title"),
                        null,
                    )

                if (confirm == Messages.YES) {
                    val settings = BonsaiSettings.getInstance()
                    settings.reset()
                    defaultExclusionListSection.loadFrom(settings)
                }
            }
        }

    val projectExclusionListButton =
        JButton(BonsaiBundle.message("settings.project.exclusion.list.button.text"), AllIcons.General.ChevronRight).apply {
            margin = BonsaiTheme.Spacing.settingsButtonMargin
            isFocusable = false
            horizontalTextPosition = SwingConstants.LEFT

            addActionListener {
                ProjectExclusionListDialog.show(
                    requireProject(),
                )
            }
        }

    val component =
        object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border =
                    JBUI.Borders.empty(
                        BonsaiTheme.Spacing.settingsBottomSectionPadding.top,
                        BonsaiTheme.Spacing.settingsBottomSectionPadding.left,
                        BonsaiTheme.Spacing.settingsBottomSectionPadding.bottom,
                        BonsaiTheme.Spacing.settingsBottomSectionPadding.right,
                    )
                add(resetButton)
                add(Box.createHorizontalGlue())
                add(projectExclusionListButton)
            }
        }

    fun isModified(settings: BonsaiSettings): Boolean = defaultExclusionListSection.isModified(settings)

    fun applyTo(settings: BonsaiSettings) {
        defaultExclusionListSection.applyTo(settings)
    }
}
