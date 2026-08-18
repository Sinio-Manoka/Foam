package com.foam.app.runtime

import com.foam.app.core.node.Node
import com.foam.app.layout.LayoutEngine
import com.foam.app.render.Renderer
import com.foam.app.style.css.Css4jStyleEngine
import org.jetbrains.skia.Canvas

class FoamRuntime(
    private val styleEngine: Css4jStyleEngine,
    private val layoutEngine: LayoutEngine,
    private val renderer: Renderer
) {

    fun renderFrame(
        canvas: Canvas,
        root: Node,
        width: Float,
        height: Float,
        scale: Float
    ) {

        styleEngine.applyStyles(
            root
        )

        layoutEngine.layout(
            root,
            width,
            height,
            scale
        )

        renderer.render(
            canvas,
            root,
            scale
        )
    }
}