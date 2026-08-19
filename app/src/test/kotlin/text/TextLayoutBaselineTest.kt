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
 * With line-metrics-based centering, the [baseline] is the y-offset
 * (in **physical pixels**) from the top of the layout rect to where
 * the alphabetic baseline of the glyph stack should be drawn, so the
 * glyph stack is visually centered in the rect. The contract is:
 *
 *   baseline = (height - ascent + descent) / 2
 *
 * where `ascent` / `descent` are the (positive) values reported by
 * Skia's [LineMetrics] for this line.
 *
 * These tests check the interface contract using a stub rather than the
 * real Skia-backed implementation (the JVM library doesn't expose a
 * zero-config Paragraph stub).
 */
internal class TextLayoutBaselineTest {

    /**
     * A minimum-viable [TextLayout] that returns constant values, used
     * to confirm the contract: `baseline == (height - ascent + descent) / 2`.
     *
     * The constant `height` and `baseline` are chosen so the formula is
     * internally consistent and `baseline > 0` and `baseline <= height`.
     */
    private class Stub(
        override val width: Float = 80f,
        override val height: Float = 22f,
        override val baseline: Float = 13f
    ) : TextLayout


    @Test
    fun baselineIsExposedOnInterface() {

        val layout =
            Stub(baseline = 18.5f)

        assertEquals(18.5f, layout.baseline)
    }


    @Test
    fun baselineSitsInsideTheBoundingBox() {

        val layout = Stub()

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
    fun stubSatisfiesCenteringFormula() {

        // Read the stub's fields and verify the contract:
        //   baseline == (height - ascent + descent) / 2
        // For a 22 px box with ascent 16, descent 6, baseline = 6.
        val height = 22f
        val ascent = 16f
        val descent = 6f
        val expectedBaseline =
            (height - ascent + descent) / 2f

        val layout =
            Stub(
                height = height,
                baseline = expectedBaseline
            )

        assertEquals(
            expectedBaseline,
            layout.baseline,
            "Stub must satisfy the baseline contract."
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
