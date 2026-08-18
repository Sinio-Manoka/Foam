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

        val paragraph =
            (node.textLayout as? SkiaTextLayout)
                ?.paragraph
                ?: createParagraph(
                    node,
                    maxWidth,
                    scale
                )

        paragraph.paint(
            canvas,
            x * scale,
            y * scale
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

        override val width: Float
            get() =
                paragraph.longestLine.toFloat()

        override val height: Float
            get() =
                paragraph.height.toFloat()
    }
}
