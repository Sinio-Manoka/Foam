package com.foam.app.dsl.components

import com.foam.app.core.view.Component
import com.foam.app.dsl.ViewScope

/**
 * A vertical flex column container.
 *
 * CSS hook: `type = "vstack"` plus whatever classes you supply (e.g.
 * `"container"`, `"sidebar"`).
 *
 * ```
 * ViewScope.VStack("container") {
 *     ViewScope.Text("Hello", "label")
 * }
 * ```
 */
fun ViewScope.VStack(
    vararg classNames: String,
    content: ViewScope.() -> Unit = {}
): VStack {

    val node =
        VStack()

    node.classes += classNames

    ViewScope(node)
        .apply(content)

    add(node)

    return node
}


/**
 * The node type for `VStack`. Currently a thin marker.
 */
class VStack : Component("vstack")
