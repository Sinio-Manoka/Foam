package com.foam.app.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The [TextLayout] interface must expose a [baseline] offset so the
 * renderer can translate the layout rect's top into Skia's baseline
 * coordinate when calling `paragraph.paint(canvas, x, y)`.
 *
 * These tests check the interface contract rather than the Skia
 * implementation directly (no JVM library stub for `Paragraph`).
 */
internal class TextLayoutBaselineTest {

    /**
     * A minimum-viable [TextLayout] that returns constant values, used
     * to confirm the interface has the new [baseline] property.
     */
    private class Stub(
        override val width: Float = 80f,
        override val height: Float = 22f,
        override val baseline: Float = 16f
    ) : TextLayout


    @Test
    fun baselineIsExposedOnInterface() {

        val layout =
            Stub(baseline = 18.5f)

        assertEquals(18.5f, layout.baseline)
    }


    @Test
    fun baselineIsAlwaysSane() {

        val layout = Stub()

        // For real text the baseline should be > 0 and <= height.
        assertTrue(
            layout.baseline > 0f,
            "baseline should be positive, got ${layout.baseline}"
        )

        assertTrue(
            layout.baseline <= layout.height,
            "baseline should be inside the bounding box " +
                "(baseline=${layout.baseline}, height=${layout.height})"
        )
    }


    @Test
    fun stubImplementsInterface() {

        // Compile-time check that the Stub class actually conforms to
        // the updated interface. If the interface loses `baseline` this
        // line fails to compile.
        val asLayout: TextLayout = Stub()
        assertNotNull(asLayout)
    }
}
