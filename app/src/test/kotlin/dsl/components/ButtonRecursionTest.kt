package com.foam.app.dsl.components

import com.foam.app.core.node.ElementNode
import com.foam.app.core.node.TextNode
import com.foam.app.core.view.Component
import com.foam.app.dsl.ViewScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression tests for the StackOverflowError that previously happened when
 * the DSL factory and the node class shared the same name.
 *
 * The bug was: `ViewScope.Button(...)` is an extension function, and inside
 * the function body the bare identifier `Button()` resolved to the
 * extension function (via the implicit receiver), not to the constructor.
 * The fix was to rename the class to `ButtonNode`.
 *
 * These tests construct Button nodes through the DSL and confirm that:
 *  - the function returns without recursing,
 *  - the returned node is an instance of `ButtonNode` (a `Component`),
 *  - the returned node carries the supplied classes.
 *
 * The tests run `ViewScope.Button(...)` inside a `ViewScope { ... }`
 * receiver just like the production app does — there is no way to call
 * a `ViewScope` extension function from outside such a block.
 */
internal class ButtonRecursionTest {

    /** A throwaway root that lets us stand up a [ViewScope] for each test. */
    private fun rootScope(): Pair<ElementNode, ViewScope> {

        val root =
            ElementNode("root")

        return root to ViewScope(root)
    }


    @Test
    fun buttonFactoryReturnsButtonNode() {

        val (_, scope) = rootScope()

        val built =
            scope.Button(
                "primary"
            )

        assertNotNull(built)
        assertEquals("button", built.type)
        assertEquals(listOf("primary"), built.classes.toList())
    }


    @Test
    fun buttonFactoryWithContentDoesNotRecurse() {

        val (_, scope) = rootScope()

        val outer =
            scope.Button("outer") {

                Text(
                    "Hello",
                    "label"
                )
            }

        assertEquals("button", outer.type)
        assertEquals(1, outer.children.size)

        val child = outer.children.first()
        assertTrue(
            child is TextNode,
            "expected TextNode, got ${child::class.simpleName}"
        )

        assertEquals("Hello", child.text)
    }


    @Test
    fun buttonIsAComponent() {

        val (_, scope) = rootScope()

        val built = scope.Button()
        val asComponent: Component = built
        assertEquals("button", asComponent.type)
    }
}
