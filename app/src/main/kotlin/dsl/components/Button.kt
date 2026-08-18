package com.foam.app.dsl.components

import com.foam.app.core.view.Component
import com.foam.app.dsl.ViewScope

/**
 * A clickable button.
 *
 * CSS hook: the node carries the `type = "button"` tag and whatever CSS
 * classes you attach via [classNames] (e.g. `"primary"`, `"secondary"`).
 *
 * The factory below is a `ViewScope` extension so it works in any
 * `ViewScope { ... }` block:
 *
 * ```
 * ViewScope.Button("primary") {
 *     ViewScope.Text("Continue", "label")
 * }
 * ```
 *
 * Implementation note: the function name (`Button`) and the underlying
 * node class ([ButtonNode]) deliberately do not share a name. Inside
 * the function body the bare identifier `Button()` resolves to the
 * extension function (via the implicit `ViewScope` receiver), not to
 * a constructor — so the underlying class is called `ButtonNode`.
 */
fun ViewScope.Button(
    vararg classNames: String,
    content: ViewScope.() -> Unit = {}
): ButtonNode {

    val node =
        ButtonNode()

    node.classes += classNames

    ViewScope(node)
        .apply(content)

    add(node)

    return node
}


/**
 * The node type for `Button`. Components grow state (e.g. `checked`,
 * `pressed`, focus-ring tracking) by adding properties here.
 *
 * Renamed from `Button` to `ButtonNode` to avoid colliding with the
 * `ViewScope.Button` extension function. The DSL name `Button(...)`
 * is what users type; the class name `ButtonNode` is internal.
 */
open class ButtonNode : Component("button")
