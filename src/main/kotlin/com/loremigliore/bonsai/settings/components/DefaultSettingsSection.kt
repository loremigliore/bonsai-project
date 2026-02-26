package com.loremigliore.bonsai.settings.components

import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.domain.enums.TreeLayout
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSeparator
import javax.swing.SwingConstants

class DefaultSettingsSection {
    private fun settings() = BonsaiSettings.getInstance()

    private val hideFoldersCheckbox =
        JCheckBox(BonsaiBundle.message("settings.hide.folders")).apply {
            isSelected = settings().hideFolders
        }

    private val hideFilesCheckbox =
        JCheckBox(BonsaiBundle.message("settings.hide.files")).apply {
            isSelected = settings().hideFiles
        }

    private val hideMiddlePackagesCheckbox =
        JCheckBox(BonsaiBundle.message("settings.hide.middle.packages")).apply {
            isSelected = settings().hideMiddlePackages
        }

    private val shrinkMiddlePackagesCheckbox =
        JCheckBox(BonsaiBundle.message("settings.shrink.middle.packages")).apply {
            isSelected = settings().shrinkMiddlePackages
        }

    private val checkboxGroup =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.emptyRight(32)

            add(hideFoldersCheckbox)
            add(Box.createVerticalStrut(6))
            add(hideFilesCheckbox)
            add(Box.createVerticalStrut(6))
            add(hideMiddlePackagesCheckbox)
            add(Box.createVerticalStrut(6))
            add(shrinkMiddlePackagesCheckbox)
        }

    private val detailGroup = ButtonGroup()

    private val simplifiedRadio = JRadioButton(BonsaiBundle.message("tree.detail.simplified"))
    private val baseRadio = JRadioButton(BonsaiBundle.message("tree.detail.base"))
    private val infraRadio = JRadioButton(BonsaiBundle.message("tree.detail.infra"))

    private val layoutGroup = ButtonGroup()

    private val cascadeRadio = JRadioButton(BonsaiBundle.message("tree.layout.cascade"))
    private val diagramRadio = JRadioButton(BonsaiBundle.message("tree.layout.diagram"))

    private fun radioRow(
        title: String,
        buttons: List<JRadioButton>,
    ): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(JLabel(title))
            add(Box.createHorizontalGlue())

            buttons.forEach {
                add(it)
                add(Box.createHorizontalStrut(12))
            }
        }

    private val radioPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            detailGroup.add(simplifiedRadio)
            detailGroup.add(baseRadio)
            detailGroup.add(infraRadio)

            add(radioRow(BonsaiBundle.message("settings.tree.detail.label"), listOf(simplifiedRadio, baseRadio, infraRadio)))
            add(Box.createVerticalStrut(12))

            layoutGroup.add(cascadeRadio)
            layoutGroup.add(diagramRadio)

            add(radioRow(BonsaiBundle.message("settings.tree.layout.label"), listOf(cascadeRadio, diagramRadio)))
        }

    val component =
        object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = JBUI.Borders.empty(4, 16)
                add(checkboxGroup)
                add(Box.createHorizontalGlue())
                add(JSeparator(SwingConstants.VERTICAL))
                add(Box.createHorizontalGlue())
                add(radioPanel)
            }

            override fun getMaximumSize(): Dimension {
                val pref = preferredSize
                return Dimension(Int.MAX_VALUE, pref.height)
            }
        }

    fun loadFrom(settings: BonsaiSettings) {
        hideFoldersCheckbox.isSelected = settings.hideFolders
        hideFilesCheckbox.isSelected = settings.hideFiles
        hideMiddlePackagesCheckbox.isSelected = settings.hideMiddlePackages
        shrinkMiddlePackagesCheckbox.isSelected = settings.shrinkMiddlePackages

        when (settings.treeDetail) {
            TreeDetail.SIMPLIFIED -> simplifiedRadio.isSelected = true
            TreeDetail.BASE -> baseRadio.isSelected = true
            TreeDetail.INFRA -> infraRadio.isSelected = true
        }

        when (settings.treeLayout) {
            TreeLayout.CASCADE -> cascadeRadio.isSelected = true
            TreeLayout.DIAGRAM -> diagramRadio.isSelected = true
        }
    }

    fun applyTo(settings: BonsaiSettings) {
        settings.hideFolders = hideFoldersCheckbox.isSelected
        settings.hideFiles = hideFilesCheckbox.isSelected
        settings.hideMiddlePackages = hideMiddlePackagesCheckbox.isSelected
        settings.shrinkMiddlePackages = shrinkMiddlePackagesCheckbox.isSelected

        settings.treeDetail =
            when {
                simplifiedRadio.isSelected -> TreeDetail.SIMPLIFIED
                infraRadio.isSelected -> TreeDetail.INFRA
                else -> TreeDetail.BASE
            }

        settings.treeLayout =
            if (diagramRadio.isSelected) {
                TreeLayout.DIAGRAM
            } else {
                TreeLayout.CASCADE
            }
    }

    fun isModified(settings: BonsaiSettings): Boolean =
        hideFoldersCheckbox.isSelected != settings.hideFolders ||
            hideFilesCheckbox.isSelected != settings.hideFiles ||
            hideMiddlePackagesCheckbox.isSelected != settings.hideMiddlePackages ||
            shrinkMiddlePackagesCheckbox.isSelected != settings.shrinkMiddlePackages
}
