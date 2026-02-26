package com.loremigliore.bonsai.ui.theme

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.loremigliore.bonsai.tree.utils.Padding
import java.awt.Color
import java.awt.Font

object BonsaiTheme {
    object Colors {
        private const val RGB = 0xD9DBE2
        private const val DARK_RGB = 0x474A4F

        val border = JBColor(RGB, DARK_RGB)

        val segmentedButtonSelected = JBColor(0xF7F8FA, 0x393B40)
        val segmentedButtonSelectedBorder = JBColor(0xD9DBE2, 0x474A4F)

        val depthSliderLabel = JBColor(Color(0x6C707E), Color(0xCED0D6))

        val minimapViewport =
            JBColor(
                Color(255, 255, 255, 30),
                Color(100, 100, 100, 50),
            )
        val treeItemSelected = JBColor(0x59A869, 0x499C54)
        val searchHighlightBg = JBColor(0x389FD6, 0x3592C4)
        val searchHighlightFg = JBColor(0x1A1A1A, 0xFFFFFF)

        val treeConnection = JBColor(RGB, DARK_RGB) as Color

        fun folderBorder(depth: Int): Color {
            val hue = (depth * 0.12f) % 1f
            return Color.getHSBColor(hue, 0.55f, 0.85f)
        }

        fun folderText(depth: Int): Color {
            val hue = (depth * 0.12f) % 1f
            return Color.getHSBColor(hue, 0.45f, 0.95f)
        }
    }

    object Typography {
        val fileFont: Font get() = JBUI.Fonts.create("JetBrains Mono", 16)
        val folderFont: Font get() = JBUI.Fonts.create("JetBrains Mono", 14)
        val searchbarFont: Font get() = JBUI.Fonts.create("JetBrains Mono", 12)
    }

    object Spacing {
        val dropdownButtonMargin = JBUI.insets(4, 10)
        val segmentedButtonPadding = JBUI.insets(1)
        val settingsButtonMargin = JBUI.insets(4, 10)
        val settingsBottomSectionPadding = JBUI.insets(16, 24, 0, 24)

        val treeContainerPadding =
            Padding(
                top = JBUI.scale(6),
                right = JBUI.scale(12),
                bottom = JBUI.scale(6),
                left = JBUI.scale(12),
            )

        val cascadeTitleMargin = JBUI.insets(10)
        val cascadeContainerMargin = JBUI.insets(10, 160, 10, 10)
        val diagramTitleMargin = JBUI.insets(10)
        val diagramContainerMargin = JBUI.insets(120, 10, 10, 10)

        val treeConnectionStroke = JBUI.scale(2).toFloat()
        val treeConnectionVerticalSpacing = JBUI.scale(10)
    }

    object Dimensions {
        // Minimap
        const val MINIMAP_WIDTH = 128
        const val MINIMAP_HEIGHT = 128
        const val MINIMAP_MARGIN = 24

        const val MINIMAP_INNER_PADDING = 8

        // Slider
        const val SLIDER_LABEL_WIDTH = 28
        const val SLIDER_THUMB_DIAMETER = 10

        // Canvas button

        const val CANVAS_BUTTON_WIDTH = 24
        const val CANVAS_BUTTON_HEIGHT = 24

        const val CANVAS_BUTTON_ARC_RADIUS = 12

        // Searchbar
        const val SEARCHBAR_WIDTH = 120
        const val SEARCHBAR_HEIGHT = 32
        const val SEARCHBAR_ARC_RADIUS = 12
        const val SEARCHBAR_TEXTFIELD_HEIGHT = 24
        const val SEARCHBAR_LABEL_HEIGHT = 18

        // Depth slider
        const val DEPTH_SLIDER_PREFERRED_WIDTH = 300
        const val DEPTH_SLIDER_MINIMUM_WIDTH = 120
        const val DEPTH_SLIDER_MAXIMUM_WIDTH = 520
        const val DEPTH_SLIDER_HEIGHT = 24

        // Segmented button
        val segmentedButtonArcRadius = JBUI.scale(10)
        val segmentedButtonBorderWidth = JBUI.scale(1).toFloat()

        // Tree
        val treeArcRadius: Int get() = JBUI.scale(8)
        val treeBorderWidth: Float get() = JBUI.scale(2).toFloat()
    }

    object Layout {
        val cascadeRootSpacing: Int get() = JBUI.scale(80)
        val cascadeVerticalSpacing: Int get() = JBUI.scale(10)

        val diagramRootSpacing: Int get() = JBUI.scale(80)
        val diagramHorizontalSpacing: Int get() = JBUI.scale(10)

        const val TREE_CONNECTION_SPLIT_RATIO = 0.75f
    }

    object Canvas {
        const val ZOOM_PADDING = 36
        const val ZOOM_MIN_SCALE = 0.006f
        const val ZOOM_MAX_SCALE = 1.4f
        const val ZOOM_IN_FACTOR = 1.1f
        const val ZOOM_OUT_FACTOR = 1f / ZOOM_IN_FACTOR

        const val PAN_TRACKPAD_SENSITIVITY = 10

        const val PAN_DRAG_THRESHOLD = 4

        const val PAN_OFFSET_THRESHOLD = 1f
        const val PAN_SCALE_THRESHOLD = 0.01f
    }

    object Components {
        val dropdownBorder = DrawUtils.CustomRoundedLineBorder()
    }

    object Export {
        const val PREVIEW_WIDTH = 700
        const val PREVIEW_HEIGHT = 500
        const val PREVIEW_PADDING = 12
        const val SCROLL_WIDTH = 720
        const val SCROLL_HEIGHT = 520

        private const val RGB = 0xD9DBE2
        private const val DARK_RGB = 0x474A4F

        val previewBackground: Color = JBColor(RGB, DARK_RGB)

        const val DEFAULT_FORMAT = "PNG"
    }
}
