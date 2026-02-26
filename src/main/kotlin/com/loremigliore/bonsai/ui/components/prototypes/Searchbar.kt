package com.loremigliore.bonsai.ui.components.prototypes

import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.logic.utils.canvasToolbarAction
import com.loremigliore.bonsai.logic.utils.searchAction
import com.loremigliore.bonsai.logic.utils.visualizerState
import com.loremigliore.bonsai.ui.icons.PluginIcons
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import com.loremigliore.bonsai.ui.theme.DrawUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class Searchbar(
    private val project: Project,
) {
    private val searchbarFont = BonsaiTheme.Typography.searchbarFont
    private val countLabel =
        object : JLabel("") {
            override fun paintComponent(g: Graphics) {
                DrawUtils.fillRoundedBackground(g, this, JBUI.scale(BonsaiTheme.Dimensions.SEARCHBAR_ARC_RADIUS))
                super.paintComponent(g)
            }
        }.apply {
            isOpaque = false
            font = searchbarFont
            isVisible = false
            horizontalAlignment = SwingConstants.CENTER
            border =
                BorderFactory.createCompoundBorder(
                    DrawUtils.CustomRoundedLineBorder(),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2),
                )
        }
    private val nextMatch = CanvasButton(PluginIcons.NextMatch) { project.searchAction.nextMatch() }
    private val exitSearch =
        CanvasButton(PluginIcons.Exit) {
            textField.text = ""
            project.canvasToolbarAction.setShowSearchbar(false)
        }
    private val textField =
        JTextField().apply {
            isOpaque = false
            border = null
            font = searchbarFont
            maximumSize = Dimension(Int.MAX_VALUE, BonsaiTheme.Dimensions.SEARCHBAR_TEXTFIELD_HEIGHT)
            minimumSize = Dimension(0, BonsaiTheme.Dimensions.SEARCHBAR_TEXTFIELD_HEIGHT)
            document.addDocumentListener(
                object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent?) = onQueryChanged(text)

                    override fun removeUpdate(e: DocumentEvent?) = onQueryChanged(text)

                    override fun changedUpdate(e: DocumentEvent?) = onQueryChanged(text)
                },
            )
        }
    private val panel =
        object : JPanel() {
            private val arc = JBUI.scale(BonsaiTheme.Dimensions.SEARCHBAR_ARC_RADIUS)

            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                isOpaque = false
                border = DrawUtils.CustomRoundedLineBorder()
                minimumSize = Dimension(BonsaiTheme.Dimensions.SEARCHBAR_WIDTH, BonsaiTheme.Dimensions.SEARCHBAR_HEIGHT)
                preferredSize = Dimension(BonsaiTheme.Dimensions.SEARCHBAR_WIDTH, BonsaiTheme.Dimensions.SEARCHBAR_HEIGHT)
                add(Box.createHorizontalStrut(3))
                add(exitSearch.component)
                add(textField)
                add(nextMatch.component)
                add(Box.createHorizontalStrut(3))
            }

            override fun paintComponent(g: Graphics) {
                DrawUtils.fillRoundedBackground(g, this, arc)
                super.paintComponent(g)
            }

            override fun getMaximumSize() = Dimension(preferredSize.width, BonsaiTheme.Dimensions.SEARCHBAR_HEIGHT)
        }

    val component: JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            add(panel)
            add(countLabel)
            preferredSize = Dimension(BonsaiTheme.Dimensions.SEARCHBAR_WIDTH, BonsaiTheme.Dimensions.SEARCHBAR_HEIGHT)
            minimumSize = Dimension(BonsaiTheme.Dimensions.SEARCHBAR_WIDTH, BonsaiTheme.Dimensions.SEARCHBAR_HEIGHT)
        }

    init {
        project.searchAction.state
            .onEach { state ->
                val text = if (state.hasMatches) "${state.currentIndex + 1}/${state.totalMatches}" else null
                countLabel.text = text ?: ""
                countLabel.isVisible = text != null
                val labelHeight = if (text != null) JBUI.scale(BonsaiTheme.Dimensions.SEARCHBAR_LABEL_HEIGHT) else 0
                val totalHeight = BonsaiTheme.Dimensions.SEARCHBAR_HEIGHT + labelHeight
                component.preferredSize = Dimension(BonsaiTheme.Dimensions.SEARCHBAR_WIDTH, totalHeight)
                component.minimumSize = Dimension(BonsaiTheme.Dimensions.SEARCHBAR_WIDTH, totalHeight)
                component.revalidate()
                component.repaint()
                component.parent?.revalidate()
                component.parent?.repaint()
            }.launchIn(project.visualizerState.cs)
    }

    fun requestFocus() {
        textField.requestFocusInWindow()
    }

    private fun onQueryChanged(text: String) {
        project.searchAction.setQuery(text.ifEmpty { null })
    }
}
