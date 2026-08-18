package com.foam.app.core.view

import com.foam.app.core.node.ElementNode

/**
 * Base class for every DSL component (Button, Text, VStack, Image, ...).
 *
 * A [Component] is just an [ElementNode] that carries:
 *   - a stable `type` tag (the HTML-ish name, e.g. `"button"`, `"vstack"`)
 *   - the class names used to look up CSS rules
 *
 * Subclasses may add typed properties for component-specific state
 * (e.g. a `Checkbox` could carry `checked: Boolean`).
 *
 * All components are constructed through the factories in `dsl/components/`,
 * which use the [newNode] helper to apply class names consistently. The
 * factories then attach the new node to the current `ViewScope` and run
 * the content lambda themselves; [Component] itself stays free of any
 * dependency on the DSL layer.
 */
open class Component(
    type: String
) : ElementNode(type) {

    companion object {

        /**
         * Build a fresh [ElementNode] of [type] with [classNames] applied.
         * Used by DSL factories so the "create a node and tag it with CSS
         * classes" step lives in exactly one place.
         */
        fun newNode(
            type: String,
            classNames: Array<out String>
        ): ElementNode {

            val node =
                ElementNode(type)

            node.classes += classNames

            return node
        }
    }
}
