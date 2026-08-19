package com.foam.app.style

import com.foam.app.style.flex.AlignItems
import com.foam.app.style.flex.Display
import com.foam.app.style.flex.FlexDirection
import com.foam.app.style.flex.JustifyContent
import com.foam.app.style.text.Direction

data class ComputedStyle(
    var width: Float? = null,
    var height: Float? = null,

    var gap: Float = 0f,

    var backgroundColor: Int? = null,
    var borderRadius: Float = 0f,

    var fontSize: Float = 16f,
    var textColor: Int = 0xFF000000.toInt(),
    var direction: Direction = Direction.LTR,

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
     * Overlay [inline] on top of this style. Only fields whose inline
     * value **differs from the property's default** are applied; this
     * keeps a no-op inline style (all defaults) from clobbering the
     * values a stylesheet set.
     *
     * Why each default matters:
     *  - Nullable fields ([width], [height], [backgroundColor]) use
     *    `null` as the "no override" sentinel — the same convention CSS
     *    uses. When the inline value is `null`, the CSS value is kept.
     *  - Numeric fields default to `0f`. A fluent `.padding(0)` would
     *    therefore not be distinguishable from a no-op, but CSS only
     *    emits `.padding(0)` when the user wrote zero padding, so this
     *    is fine.
     *  - Enum fields ([display], [flexDirection], [justifyContent],
     *    [alignItems], [direction]) use their property defaults as the
     *    "no override" sentinel.
     *
     * This is the only safe merge semantics given that the inline
     * style field defaults are reused as the "absent" sentinel — the
     * inline style was deliberately never explicitly constructed by
     * the user in the typical `Button("primary") { ... }` flow.
     */
    fun mergeInline(
        inline: ComputedStyle
    ) {

        // Nullable fields: only override when inline is non-null.
        if (inline.width != null) width = inline.width
        if (inline.height != null) height = inline.height
        if (inline.backgroundColor != null) backgroundColor = inline.backgroundColor

        // Numeric fields: 0f is the "no override" sentinel.
        if (inline.gap != 0f) gap = inline.gap
        if (inline.borderRadius != 0f) borderRadius = inline.borderRadius
        // fontSize defaults to 16f. We don't have a sentinel for
        // "leave font-size alone", so accept the limitation: a fluent
        // .fontSize(16) will be a no-op. (16px is also the universal
        // browser default, so this is rarely a problem.)
        if (inline.fontSize != 16f) fontSize = inline.fontSize
        if (inline.textColor != 0xFF000000.toInt()) textColor = inline.textColor

        if (inline.paddingTop != 0f) paddingTop = inline.paddingTop
        if (inline.paddingRight != 0f) paddingRight = inline.paddingRight
        if (inline.paddingBottom != 0f) paddingBottom = inline.paddingBottom
        if (inline.paddingLeft != 0f) paddingLeft = inline.paddingLeft

        if (inline.marginTop != 0f) marginTop = inline.marginTop
        if (inline.marginRight != 0f) marginRight = inline.marginRight
        if (inline.marginBottom != 0f) marginBottom = inline.marginBottom
        if (inline.marginLeft != 0f) marginLeft = inline.marginLeft

        // Enum fields: each enum's default is the "no override" sentinel.
        if (inline.display != Display.BLOCK) display = inline.display
        if (inline.flexDirection != FlexDirection.ROW) flexDirection = inline.flexDirection
        if (inline.justifyContent != JustifyContent.START) justifyContent = inline.justifyContent
        if (inline.alignItems != AlignItems.STRETCH) alignItems = inline.alignItems
        if (inline.direction != Direction.LTR) direction = inline.direction
    }
}
