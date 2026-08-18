package com.foam.app.dsl

import com.foam.app.core.node.TextNode

fun ViewScope.Text(
    value: String,
    vararg classNames: String
): TextNode {

    val node =
        TextNode(value)

    node.classes += classNames

    add(node)

    return node
}