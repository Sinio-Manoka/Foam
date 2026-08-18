package com.foam.app.core.view

import com.foam.app.core.node.ElementNode
import com.foam.app.dsl.ViewScope

/**
 * Base class for every DSL component (Button, Text, VStack, Image, ...).
 *
 * A [Component] is just an [ElementNode] that carries:
 *   - a stable `type` tag (the HTML-ish name, e.g. `"button"`, `"vstack"`)
 *   - the class names used to look up CSS rules
 *
 * Subclasses add typed properties for component-specific state
 * (e.g. a `Checkbox` could carry `checked: Boolean`).
 *
 * Construction is handled by the DSL factories in `dsl/components/`,
 * which use [newNode] as a helper to apply class names consistently.
 */
open class Component(
    type: String
) : ElementNode(type) {

    companion object {

        /**
         * Build a fresh [ElementNode] of [type] with [classNames] applied
         * and run [content] against the new node's view scope.
         *
         * Use this for one-off elements that don't have a typed subclass.
         */
        fun newNode(
            type: String,
            classNames: Array<out String>,
            content: (ViewScope.() -> Unit)? = null
        ): ElementNode {

            val node =
                ElementNode(type)

            node.classes += classNames

            content?.let { block ->
                ViewScope(node)
                    .apply(block)
            }

            return node
        }
    }
}
