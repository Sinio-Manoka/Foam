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
 *
 * Implementation note: the function name (`VStack`) and the underlying
 * node class ([VStackNode]) deliberately do not share a name. Inside
 * the function body the bare identifier `VStack()` resolves to the
 * extension function (via the implicit `ViewScope` receiver), not to
 * a constructor — so the underlying class is called `VStackNode`.
 */
fun ViewScope.VStack(
    vararg classNames: String,
    content: ViewScope.() -> Unit = {}
): VStackNode {

    val node =
        VStackNode()

    node.classes += classNames

    ViewScope(node)
        .apply(content)

    add(node)

    return node
}


/**
 * The node type for `VStack`. Currently a thin marker.
 */
open class VStackNode : Component("vstack")
