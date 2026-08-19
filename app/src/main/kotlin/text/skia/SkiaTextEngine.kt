package com.foam.app.text.skia

import com.foam.app.core.node.TextNode
import com.foam.app.style.text.Direction
import com.foam.app.text.TextEngine
import com.foam.app.text.TextLayout

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction as SkiaDirection
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle

class SkiaTextEngine : TextEngine {

    private val fontCollection =
        FontCollection().apply {
            setDefaultFontManager(
                FontMgr.default
            )
        }


    override fun measure(
        node: TextNode,
        maxWidth: Float,
        scale: Float
    ): TextLayout {

        val layout =
            SkiaTextLayout(
                createParagraph(
                    node,
                    maxWidth,
                    scale
                )
            )

        node.textLayout =
            layout

        return layout
    }


    override fun paint(
        canvas: Canvas,
        node: TextNode,
        x: Float,
        y: Float,
        maxWidth: Float,
        scale: Float
    ) {

        val stored =
            (node.textLayout as? SkiaTextLayout)
                ?.paragraph
                ?: createParagraph(
                    node,
                    maxWidth,
                    scale
                )

        // Skia's `paragraph.paint(canvas, x, y)` paints with the baseline
        // of the first line at `(x, y)`, not the top-left of the box.
        // The cached layout stores the baseline distance (in physical
        // pixels) from the top of the bounding box; convert it back to
        // logical by dividing by the device scale before adding to the
        // top-y the layout engine gave us.
        val baselinePx =
            (node.textLayout as? SkiaTextLayout)
                ?.baseline
                ?: 0f

        val baselineLogical =
            if (scale > 0f) baselinePx / scale else 0f

        stored.paint(
            canvas,
            x * scale,
            (y + baselineLogical) * scale
        )
    }


    private fun createParagraph(
        node: TextNode,
        maxWidth: Float,
        scale: Float
    ): Paragraph {

        val style =
            node.computedStyle

        val textStyle =
            TextStyle().apply {

                fontFamilies =
                    arrayOf("Segoe UI")

                fontSize =
                    style.fontSize * scale

                color =
                    style.textColor
            }

        val paragraphStyle =
            ParagraphStyle().apply {

                this.textStyle =
                    textStyle

                alignment =
                    Alignment.CENTER

                maxLinesCount =
                    1

                ellipsis =
                    null

                // Direction: CSS `direction: rtl` maps to RTL bidi
                // shaping so Arabic / Hebrew text reads naturally. ICU
                // handles the per-glyph bidi resolution automatically.
                direction =
                    when (style.direction) {
                        Direction.LTR -> SkiaDirection.LTR
                        Direction.RTL -> SkiaDirection.RTL
                    }
            }

        return ParagraphBuilder(
            paragraphStyle,
            fontCollection
        ).run {

            addText(
                node.text
            )

            build()
        }.also {

            val width =
                if (maxWidth > 0f) {
                    maxWidth * scale
                } else {
                    Float.POSITIVE_INFINITY
                }

            it.layout(
                width
            )
        }
    }


    private class SkiaTextLayout(
        val paragraph: Paragraph
    ) : TextLayout {

        /**
         * The physical-pixel y-offset at which to draw the **baseline** of
         * the first line so its visual bbox is centered in the leaf's
         * layout rect.
         *
         * We compute this from per-line metrics rather than blindly using
         * `paragraph.alphabeticBaseline` because that value sits inside a
         * paragraph box whose height includes leading and whose top is
         * not necessarily the glyph's visual top — line metrics expose
         * the ascent/descent values that define the actual glyph bbox.
         *
         * For an empty paragraph (no lines), the offset falls back to
         * the alphabetic baseline so callers don't have to special-case
         * empty text.
         */
        override val baseline: Float by lazy {

            val lineMetrics =
                paragraph.lineMetrics

            if (lineMetrics.isEmpty()) {

                paragraph.alphabeticBaseline
            } else {

                val line = lineMetrics[0]

                // LineMetrics describes the glyph's visual bbox:
                //   top    = baseline - ascent
                //   bottom = baseline + descent
                // Ascent and descent are positive. The visual height of
                // the glyph stack is `ascent + descent`.
                val ascent =
                    line.ascent.toFloat()
                val descent =
                    line.descent.toFloat()

                // `paragraph.height` includes any leading above and below
                // the glyph stack. Compute the visual center of the leaf,
                // then snap to the baseline by adding half the visual
                // height minus half the ascent (i.e. walk from the visual
                // center down to the baseline).
                val leafHeight =
                    paragraph.height

                val visualCenter =
                    (leafHeight - ascent + descent) / 2f

                visualCenter
            }
        }


        override val width: Float
            get() =
                paragraph.longestLine.toFloat()


        override val height: Float
            get() =
                paragraph.height.toFloat()
    }
}
