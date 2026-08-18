package com.foam.app.dsl

import com.foam.app.core.node.ElementNode

fun ViewScope.Button(
    vararg classNames: String,
    content: ViewScope.() -> Unit
): ElementNode {

    val node =
        ElementNode("button")

    node.classes += classNames

    ViewScope(node)
        .apply(content)

    add(node)

    return node
}