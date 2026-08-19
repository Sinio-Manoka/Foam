package com.foam.app.style

import com.foam.app.style.flex.AlignItems
import com.foam.app.style.flex.Display
import com.foam.app.style.flex.FlexDirection
import com.foam.app.style.flex.JustifyContent

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
) {

    /**
     * Overlay [inline] on top of this style, applying inline overrides.
     *
     * Semantics:
     *  - nullable fields (e.g. [width]) are overridden when the inline value
     *    is also non-null.
     *  - non-null fields (numeric) override unconditionally.
     *  - this lets the DSL fluent modifiers (`.padding(8)`, `.frame(...)`)
     *    take effect, while leaving CSS classes untouched when the inline
     *    side doesn't mention them.
     */
    fun mergeInline(
        inline: ComputedStyle
    ) {

        if (inline.width != null) width = inline.width
        if (inline.height != null) height = inline.height

        gap = inline.gap

        if (inline.backgroundColor != null) backgroundColor = inline.backgroundColor
        borderRadius = inline.borderRadius

        fontSize = inline.fontSize
        textColor = inline.textColor

        display = inline.display
        flexDirection = inline.flexDirection
        justifyContent = inline.justifyContent
        alignItems = inline.alignItems

        marginTop = inline.marginTop
        marginRight = inline.marginRight
        marginBottom = inline.marginBottom
        marginLeft = inline.marginLeft

        paddingTop = inline.paddingTop
        paddingRight = inline.paddingRight
        paddingBottom = inline.paddingBottom
        paddingLeft = inline.paddingLeft
    }
}
