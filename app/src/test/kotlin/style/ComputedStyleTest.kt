package com.foam.app.style

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Regression tests for [ComputedStyle.mergeInline] — particularly the
 * "default-merge wipes out CSS value" footgun that previously hid the
 * button background.
 *
 * Bug history: mergeInline unconditionally assigned every field from
 * the inline style. Defaults like `backgroundColor = null` thus
 * overrode CSS-derived values, making elements invisible (the renderer
 * returns early when `backgroundColor` is null).
 */
internal class ComputedStyleTest {

    @Test
    fun mergeInlinePreservesCssBackgroundColorWhenInlineIsNull() {

        val css =
            ComputedStyle().apply {
                backgroundColor = 0xFF008CBA.toInt()
            }

        val inline =
            ComputedStyle()    // backgroundColor = null

        css.mergeInline(inline)

        assertEquals(
            0xFF008CBA.toInt(),
            css.backgroundColor,
            "CSS-derived backgroundColor must survive a default mergeInline"
        )
    }


    @Test
    fun mergeInlineOverridesCssBackgroundColorWhenInlineIsSet() {

        val css =
            ComputedStyle().apply {
                backgroundColor = 0xFF000000.toInt()
            }

        val inline =
            ComputedStyle().apply {
                backgroundColor = 0xFFFF00FF.toInt()
            }

        css.mergeInline(inline)

        assertEquals(
            0xFFFF00FF.toInt(),
            css.backgroundColor,
            "Inline non-null backgroundColor must win"
        )
    }


    @Test
    fun mergeInlinePreservesCssWidthWhenInlineIsNull() {

        val css =
            ComputedStyle().apply {
                width = 200f
            }

        val inline =
            ComputedStyle()    // width = null

        css.mergeInline(inline)

        assertEquals(200f, css.width)
    }


    @Test
    fun defaultsAreCoherent() {

        val default = ComputedStyle()

        assertNull(default.backgroundColor)
        assertNull(default.width)
        assertNull(default.height)
        assertEquals(0f, default.gap)
        assertEquals(0f, default.borderRadius)
        assertEquals(16f, default.fontSize)

        // sanity: notNull sanity check on enum default
        assertNotNull(default.display)
    }
}
