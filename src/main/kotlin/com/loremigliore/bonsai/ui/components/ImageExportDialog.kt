package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.ui.components.prototypes.BonsaiDialog
import com.loremigliore.bonsai.ui.theme.BonsaiTheme
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.Action
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

object ImageExportDialog {
    fun show(
        project: Project,
        image: BufferedImage,
    ) {
        val dialog = InternalDialog(project, image)
        if (dialog.showAndGet()) {
            dialog.saveImage()
        }
    }

    private class InternalDialog(
        private val project: Project,
        private val image: BufferedImage,
    ) : BonsaiDialog(BonsaiBundle.message("image_export_dialog.window.title")) {
        private var selectedFormat = BonsaiTheme.Export.DEFAULT_FORMAT

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            val pngButton = JRadioButton("PNG").apply { isSelected = selectedFormat == "PNG" }
            val svgButton = JRadioButton("SVG").apply { isSelected = selectedFormat == "SVG" }

            ButtonGroup().apply {
                add(pngButton)
                add(svgButton)
            }

            pngButton.addItemListener { if (pngButton.isSelected) selectedFormat = "PNG" }
            svgButton.addItemListener { if (svgButton.isSelected) selectedFormat = "SVG" }

            val formatRow =
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.X_AXIS)
                    border = JBUI.Borders.empty(BonsaiTheme.Export.PREVIEW_PADDING)
                    add(Box.createHorizontalGlue())
                    add(JLabel(BonsaiBundle.message("image_export_dialog.image_format.text")))
                    add(Box.createHorizontalStrut(8))
                    add(pngButton)
                    add(Box.createHorizontalStrut(4))
                    add(svgButton)
                    add(Box.createHorizontalGlue())
                }

            val previewPanel =
                object : JPanel() {
                    override fun paintComponent(g: Graphics) {
                        super.paintComponent(g)
                        val g2 = g as Graphics2D
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

                        val scaleX = width.toDouble() / image.width
                        val scaleY = height.toDouble() / image.height
                        val scale = minOf(scaleX, scaleY, 1.0)

                        val drawWidth = (image.width * scale).toInt()
                        val drawHeight = (image.height * scale).toInt()
                        val drawX = (width - drawWidth) / 2
                        val drawY = (height - drawHeight) / 2

                        g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null)
                    }

                    override fun getPreferredSize() =
                        Dimension(
                            BonsaiTheme.Export.PREVIEW_WIDTH,
                            BonsaiTheme.Export.PREVIEW_HEIGHT,
                        )
                }

            previewPanel.border = JBUI.Borders.empty(BonsaiTheme.Export.PREVIEW_PADDING)
            previewPanel.background = BonsaiTheme.Export.previewBackground

            val scroll = JBScrollPane(previewPanel)
            scroll.preferredSize =
                Dimension(
                    BonsaiTheme.Export.SCROLL_WIDTH,
                    BonsaiTheme.Export.SCROLL_HEIGHT,
                )

            val container = JPanel(BorderLayout())
            container.add(formatRow, BorderLayout.NORTH)
            container.add(scroll, BorderLayout.CENTER)

            return container
        }

        override fun createActions(): Array<out Action?> = arrayOf(okAction, cancelAction)

        fun saveImage() {
            val format = selectedFormat.lowercase()
            val descriptor =
                FileSaverDescriptor(
                    BonsaiBundle.message("image_export_dialog.save.title"),
                    BonsaiBundle.message("image_export_dialog.save.description", selectedFormat),
                    format,
                )
            val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
            val wrapper = dialog.save(null as VirtualFile?, "tree-export") ?: return
            val file = wrapper.file

            when (selectedFormat) {
                "PNG" -> {
                    ImageIO.write(image, "PNG", file)
                }

                "SVG" -> {
                    val svgContent = buildSvgFromImage(image)
                    file.writeText(svgContent)
                }
            }
        }

        private fun buildSvgFromImage(image: BufferedImage): String {
            val baos = java.io.ByteArrayOutputStream()
            ImageIO.write(image, "PNG", baos)
            val base64 =
                java.util.Base64
                    .getEncoder()
                    .encodeToString(baos.toByteArray())
            return """
                <svg xmlns="http://www.w3.org/2000/svg" width="${image.width}" height="${image.height}">
                    <image href="data:image/png;base64,$base64" width="${image.width}" height="${image.height}"/>
                </svg>
                """.trimIndent()
        }
    }
}
