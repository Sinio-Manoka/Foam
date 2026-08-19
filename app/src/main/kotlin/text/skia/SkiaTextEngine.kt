package com.foam.app.text.skia

import com.foam.app.core.node.TextNode
import com.foam.app.text.TextEngine
import com.foam.app.text.TextLayout

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.paragraph.Alignment
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
         * The y-offset (in physical pixels) from the top of the paragraph
         * bounding box down to the alphabetic baseline of the first line.
         *
         * Skia's `paragraph.paint(canvas, x, y)` paints with the baseline
         * at `(x, y)`, so the renderer has to add this offset to the
         * node's top-y to land the text inside its layout rect.
         */
        override val baseline: Float by lazy {

            paragraph.alphabeticBaseline
        }


        override val width: Float
            get() =
                paragraph.longestLine.toFloat()


        override val height: Float
            get() =
                paragraph.height.toFloat()
    }
}
