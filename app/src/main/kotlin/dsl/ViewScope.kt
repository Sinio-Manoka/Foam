package com.foam.app.dsl

import com.foam.app.core.node.Node

class ViewScope(
    private val parent: Node
) {

    fun add(
        node: Node
    ) {
        parent.add(node)
    }
}
