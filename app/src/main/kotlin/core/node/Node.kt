package com.foam.app.core.node

import com.foam.app.core.geometry.LayoutRect
import com.foam.app.style.ComputedStyle

abstract class Node {

    var parent: Node? = null

    val children =
        mutableListOf<Node>()

    val classes =
        mutableListOf<String>()

    var id: String? = null

    var layout =
        LayoutRect()

    var computedStyle =
        ComputedStyle()

    var inlineStyle =
        ComputedStyle()

    fun add(
        child: Node
    ) {
        child.parent = this
        children += child
    }
}
