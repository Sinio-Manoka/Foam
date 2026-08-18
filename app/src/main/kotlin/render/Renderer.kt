package com.foam.app.render

import com.foam.app.core.node.Node
import org.jetbrains.skia.Canvas

interface Renderer {
    fun render(
        canvas: Canvas,
        root: Node,
        scale: Float
    )
}
