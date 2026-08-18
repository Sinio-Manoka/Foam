package com.foam.app.style

enum class Display {
    FLEX,
    BLOCK,
    NONE
}

enum class FlexDirection {
    ROW,
    COLUMN
}

enum class JustifyContent {
    START,
    CENTER,
    END,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY
}

enum class AlignItems {
    START,
    CENTER,
    END,
    STRETCH
}

data class ComputedStyle(
    var width: Float? = null,
    var height: Float? = null,

    var gap: Float = 0f,

    var backgroundColor: Int? = null,
    var borderRadius: Float = 0f,

    var fontSize: Float = 16f,
    var textColor: Int = 0xFF000000.toInt(),

    var display: Display = Display.BLOCK,
    var flexDirection: FlexDirection = FlexDirection.ROW,
    var justifyContent: JustifyContent = JustifyContent.START,
    var alignItems: AlignItems = AlignItems.STRETCH,

    var marginTop: Float = 0f,
    var marginRight: Float = 0f,
    var marginBottom: Float = 0f,
    var marginLeft: Float = 0f,

    var paddingTop: Float = 0f,
    var paddingRight: Float = 0f,
    var paddingBottom: Float = 0f,
    var paddingLeft: Float = 0f
)