package com.foam.app.render.skia

import com.foam.app.core.node.ElementNode
import com.foam.app.core.node.Node
import com.foam.app.core.node.TextNode
import com.foam.app.render.Renderer
import com.foam.app.text.TextEngine

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RRect


class SkiaRenderer(
    private val textEngine: TextEngine
) : Renderer {

    override fun render(
        canvas: Canvas,
        root: Node,
        scale: Float
    ) {

        renderNode(
            canvas,
            root,
            scale
        )
    }


    private fun renderNode(
        canvas: Canvas,
        node: Node,
        scale: Float
    ) {

        when (node) {

            is ElementNode ->
                drawElement(
                    canvas,
                    node,
                    scale
                )

            is TextNode ->
                drawText(
                    canvas,
                    node,
                    scale
                )
        }

        for (child in node.children) {

            renderNode(
                canvas,
                child,
                scale
            )
        }
    }


    private fun drawText(
        canvas: Canvas,
        node: TextNode,
        scale: Float
    ) {

        textEngine.paint(
            canvas,
            node,
            node.layout.x,
            node.layout.y,
            Float.POSITIVE_INFINITY,
            scale
        )
    }


    private fun drawElement(
        canvas: Canvas,
        node: ElementNode,
        scale: Float
    ) {

        val style =
            node.computedStyle

        val color =
            style.backgroundColor
                ?: return

        Paint().use { paint ->

            paint.isAntiAlias =
                true

            paint.color =
                color

            canvas.drawRRect(
                RRect.makeXYWH(
                    node.layout.x * scale,
                    node.layout.y * scale,
                    node.layout.width * scale,
                    node.layout.height * scale,
                    style.borderRadius * scale
                ),
                paint
            )
        }
    }
}