package com.foam.app.layout

import com.foam.app.core.node.Node

interface LayoutEngine {
    fun layout(
        root: Node,
        width: Float,
        height: Float
    )
}
