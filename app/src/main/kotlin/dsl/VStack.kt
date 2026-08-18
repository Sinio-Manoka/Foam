package com.foam.app.dsl

import com.foam.app.core.node.ElementNode

fun ViewScope.VStack(
    vararg classNames: String,
    content: ViewScope.() -> Unit
): ElementNode {

    val node =
        ElementNode("vstack")

    node.classes += classNames

    ViewScope(node)
        .apply(content)

    add(node)

    return node
}
