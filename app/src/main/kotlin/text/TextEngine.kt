package com.foam.app.text

import com.foam.app.core.node.TextNode
import org.jetbrains.skia.Canvas

interface TextEngine {
    fun measure(
        node: TextNode,
        maxWidth: Float,
        scale: Float
    ): TextLayout

    fun paint(
        canvas: Canvas,
        node: TextNode,
        x: Float,
        y: Float,
        maxWidth: Float,
        scale: Float
    )
}