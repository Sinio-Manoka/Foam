package com.foam.app.dsl.components

import com.foam.app.core.node.TextNode
import com.foam.app.dsl.ViewScope

/**
 * A text node. Carries its literal string and is rendered with the
 * stylesheet rules keyed on the supplied CSS classes (e.g. `label`,
 * `heading`, `muted`).
 *
 * ```
 * ViewScope.Text("Continue", "label")
 * ```
 *
 * Unlike [Button] and [VStack], `Text` is a leaf node (`TextNode`) and
 * doesn't subclass [com.foam.app.core.view.Component] — text doesn't
 * take children.
 */
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
