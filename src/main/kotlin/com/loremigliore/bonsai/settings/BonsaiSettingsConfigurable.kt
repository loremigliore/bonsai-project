package com.loremigliore.bonsai.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.TitledSeparator
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.logic.services.settings.BonsaiSettings
import com.loremigliore.bonsai.settings.components.BottomSection
import com.loremigliore.bonsai.settings.components.DefaultExclusionListSection
import com.loremigliore.bonsai.settings.components.DefaultSettingsSection
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class BonsaiSettingsConfigurable : Configurable {
    private val defaultSettingsSection = DefaultSettingsSection()
    private val defaultExclusionListSection = DefaultExclusionListSection()
    private val bottomSection = BottomSection(defaultExclusionListSection)

    override fun getDisplayName(): String = BonsaiBundle.message("settings.display.name")

    override fun createComponent(): JComponent =
        JPanel(BorderLayout()).apply {
            val topPanel =
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)

                    add(TitledSeparator(BonsaiBundle.message("settings.default.options.title")))
                    add(Box.createVerticalStrut(8))
                    add(defaultSettingsSection.component)

                    add(Box.createVerticalStrut(16))

                    add(TitledSeparator(BonsaiBundle.message("settings.default.exclusion.list.title")))
                    add(Box.createVerticalStrut(8))
                }

            val listPanel =
                JPanel(BorderLayout()).apply {
                    add(defaultExclusionListSection.component, BorderLayout.CENTER)
                }

            val bottomPanel =
                JPanel(BorderLayout()).apply {
                    add(bottomSection.component)
                }

            add(topPanel, BorderLayout.NORTH)
            add(listPanel, BorderLayout.CENTER)
            add(bottomPanel, BorderLayout.SOUTH)
        }

    override fun reset() {
        val settings = BonsaiSettings.getInstance()
        defaultSettingsSection.loadFrom(settings)
        defaultExclusionListSection.loadFrom(settings)
    }

    override fun isModified(): Boolean {
        val settings = BonsaiSettings.getInstance()
        return defaultSettingsSection.isModified(settings) ||
            defaultExclusionListSection.isModified(settings) ||
            bottomSection.isModified(settings)
    }

    override fun apply() {
        val settings = BonsaiSettings.getInstance()
        defaultSettingsSection.applyTo(settings)
        defaultExclusionListSection.applyTo(settings)
        bottomSection.applyTo(settings)
    }
}
