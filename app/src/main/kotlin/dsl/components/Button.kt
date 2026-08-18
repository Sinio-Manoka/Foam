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
 */
fun ViewScope.Button(
    vararg classNames: String,
    content: ViewScope.() -> Unit = {}
): Button {

    val node =
        Button()

    node.classes += classNames

    ViewScope(node)
        .apply(content)

    add(node)

    return node
}


/**
 * The node type for `Button`. Currently a thin marker — components grow
 * state (e.g. `checked`, `pressed`, focus ring tracking) by adding
 * properties here.
 */
class Button : Component("button")
