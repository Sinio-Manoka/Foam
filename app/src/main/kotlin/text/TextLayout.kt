package com.foam.app.text

/**
 * The result of measuring a text node — what its layout should be.
 *
 * - [width] / [height] are the natural bounding box of the text in
 *   *physical* pixels (post-scaling).
 * - [baseline] is the y-offset (in physical pixels) from the **top of the
 *   bounding box** down to the first line's alphabetic baseline. The
 *   renderer needs this because Skia's `paragraph.paint(canvas, x, y)`
 *   places the baseline at `(x, y)`, not the top-left of the box — so
 *   the renderer has to add [baseline] to the leaf's top-y to put the
 *   text inside its layout rect instead of half a character above it.
 */
interface TextLayout {
    val width: Float
    val height: Float
    val baseline: Float
}
