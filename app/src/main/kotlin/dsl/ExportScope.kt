package com.foam.app.dsl

import com.foam.app.core.node.ElementNode

/**
 * The receiver type for the top-level `export { ... }` block of a
 * [com.foam.app.runtime.FoamApp].
 *
 * The first call inside `export { ... }` is expected to return the UI
 * root — see [VStack]. Because [ExportScope] is *not* itself a
 * [ViewScope], the factories inside it are defined directly here rather
 * than being shared with nested blocks.
 */
class ExportScope {

    fun VStack(
        vararg classNames: String,
        content: ViewScope.() -> Unit = {}
    ): ElementNode {

        val node =
            ElementNode("vstack")

        node.classes += classNames

        ViewScope(node)
            .apply(content)

        return node
    }
}
