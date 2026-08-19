package com.foam.app.layout.taffy

import com.foam.app.core.node.ElementNode
import com.foam.app.core.node.Node
import com.foam.app.core.node.TextNode
import com.foam.app.style.ComputedStyle
import com.foam.app.style.flex.AlignItems
import com.foam.app.style.flex.Display
import com.foam.app.style.flex.FlexDirection
import com.foam.app.style.flex.JustifyContent
import com.foam.app.text.TextEngine
import com.foam.app.text.TextLayout
import org.jetbrains.skia.Canvas
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for [TaffyLayoutEngine] using a stub [TextEngine].
 *
 * Catches regressions for the multiple bugs that wiped out CSS-derived
 * styles on the inline merge: when a node carries a CSS rule
 * (e.g. `display: flex; justify-content: center; align-items: center`)
 * and a default-merged inline style, the layout engine must still
 * see the CSS values.
 */
internal class TaffyLayoutIntegrationTest {

    /**
     * A minimal [TextEngine]: returns a constant text height of 21 px
     * with a 16-px baseline and 50% of `maxWidth` as the text width.
     */
    private class StubTextEngine : TextEngine {

        override fun measure(
            node: TextNode,
            maxWidth: Float,
            scale: Float
        ): TextLayout {

            val heightPx = 21f * scale
            val widthPx = (maxWidth * 0.5f) * scale

            return object : TextLayout {
                override val width: Float = widthPx / scale
                override val height: Float = heightPx / scale
                override val baseline: Float = 16f * scale
            }
        }


        override fun paint(
            canvas: Canvas,
            node: TextNode,
            x: Float,
            y: Float,
            maxWidth: Float,
            scale: Float
        ) = Unit
    }


    private fun layout(
        tree: Node,
        width: Float = 900f,
        height: Float = 600f
    ) =

        TaffyLayoutEngine(StubTextEngine())
            .also { it.layout(tree, width, height, scale = 1f) }


    /**
     * Two side-by-side buttons in a flex-row stage. Verifies the stage
     * fills the window and the buttons are centered horizontally.
     */
    @Test
    fun twoButtonsSideBySide() {

        val stage =
            ElementNode("stage").apply {
                computedStyle = ComputedStyle().apply {
                    display = Display.FLEX
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.CENTER
                    alignItems = AlignItems.CENTER
                    gap = 24f
                    width = 900f
                    height = 600f
                    backgroundColor = 0xFFF5F5F7.toInt()
                }
            }

        val buttonCss: ComputedStyle.() -> Unit = {
            display = Display.FLEX
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            paddingTop = 12f
            paddingBottom = 12f
            paddingLeft = 32f
            paddingRight = 32f
            borderRadius = 10f
            width = 200f
        }

        val buttonA =
            ElementNode("button").apply {
                computedStyle = ComputedStyle().apply {
                    buttonCss()
                    backgroundColor = 0xFF008CBA.toInt()
                }
            }

        val buttonB =
            ElementNode("button").apply {
                computedStyle = ComputedStyle().apply {
                    buttonCss()
                    backgroundColor = 0xFFAF52DE.toInt()
                }
            }

        val labelA = TextNode("Continue")
        val labelB = TextNode("يكمل")

        stage.add(buttonA); buttonA.add(labelA)
        stage.add(buttonB); buttonB.add(labelB)

        layout(stage)

        // Stage fills the window.
        assertEquals(0f, stage.layout.x)
        assertEquals(0f, stage.layout.y)
        assertEquals(900f, stage.layout.width)
        assertEquals(600f, stage.layout.height)

        // Buttons side-by-side, centered. (200 + 24 + 200 = 424,
        // (900 - 424) / 2 = 238 of left margin.)
        assertEquals(238f, buttonA.layout.x, 1.0f)
        assertEquals(
            238f + 200f + 24f,
            buttonB.layout.x,
            1.0f
        )
        assertEquals(200f, buttonA.layout.width, 0.5f)
        assertEquals(200f, buttonB.layout.width, 0.5f)

        // Each button auto-sizes to its content (label ~21 px +
        // 12+12 padding = ~45 px).
        assertTrue(
            buttonA.layout.height in 40f..50f,
            "button A height should be ~45, got ${buttonA.layout.height}"
        )
        assertTrue(
            buttonB.layout.height in 40f..50f,
            "button B height should be ~45, got ${buttonB.layout.height}"
        )

        // Each label sits inside its button.
        assertTrue(
            labelA.layout.x > buttonA.layout.x &&
                labelA.layout.x + labelA.layout.width <
                buttonA.layout.x + buttonA.layout.width,
            "label A should fit inside button A"
        )
        assertNotNull(stage.children.firstOrNull())
    }


    /**
     * Regression for the inline-style wipe-out: a node carrying CSS
     * `display: flex; justify-content: center; align-items: center`
     * survives a default [ComputedStyle.mergeInline] call and the
     * child ends up centered.
     */
    @Test
    fun cssValuesSurviveDefaultInlineMerge() {

        val container =
            ElementNode("container").apply {
                computedStyle = ComputedStyle().apply {
                    display = Display.FLEX
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.CENTER
                    alignItems = AlignItems.CENTER
                }
            }

        val child =
            ElementNode("child").apply {
                computedStyle = ComputedStyle().apply {
                    width = 100f
                }
            }

        container.add(child)

        // Mimic Css4jStyleEngine: build a fresh ComputedStyle, apply
        // CSS (already in place here), then merge the default inline.
        container.computedStyle.mergeInline(ComputedStyle())

        layout(container, width = 400f, height = 100f)

        // Child should be centered (CSS justify-content: center is
        // intact). Container is 400 wide, child is 100 wide, so the
        // child sits at x = (400 - 100) / 2 = 150.
        assertEquals(150f, child.layout.x, 1.0f)
    }


    /**
     * Regression for the earlier "mergeInline sets display = BLOCK"
     * bug: a node with CSS `display: flex` still ends up as a flex
     * container after a default mergeInline.
     */
    @Test
    fun displayFlexSurvivesDefaultInlineMerge() {

        val container =
            ElementNode("container").apply {
                computedStyle = ComputedStyle().apply {
                    display = Display.FLEX
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.CENTER
                    alignItems = AlignItems.CENTER
                }
            }

        container.computedStyle.mergeInline(ComputedStyle())

        assertEquals(
            Display.FLEX,
            container.computedStyle.display,
            "display must not be reset to BLOCK by mergeInline"
        )
        assertEquals(
            JustifyContent.CENTER,
            container.computedStyle.justifyContent,
            "justify-content must not be reset to START by mergeInline"
        )
        assertEquals(
            AlignItems.CENTER,
            container.computedStyle.alignItems,
            "align-items must not be reset to STRETCH by mergeInline"
        )
    }


    /**
     * Same trap as above but specifically about the field defaults
     * that were the bug source: the inline `ComputedStyle()` defaults
     * are BLOCK / ROW / START / STRETCH. If a node's CSS happens to
     * match those defaults (e.g. `flex-direction: row` is the same as
     * the inline default), mergeInline must still leave the CSS
     * values untouched.
     */
    @Test
    fun allFieldsSurviveDefaultInlineMerge() {

        val node =
            ElementNode("node").apply {
                computedStyle = ComputedStyle().apply {
                    display = Display.FLEX
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.START
                    alignItems = AlignItems.STRETCH
                    gap = 24f
                    paddingLeft = 16f
                    paddingRight = 16f
                    paddingTop = 8f
                    paddingBottom = 8f
                    marginTop = 4f
                    marginBottom = 4f
                    borderRadius = 10f
                    fontSize = 18f
                    backgroundColor = 0xFF112233.toInt()
                }
            }

        node.computedStyle.mergeInline(ComputedStyle())

        assertEquals(Display.FLEX, node.computedStyle.display)
        assertEquals(FlexDirection.ROW, node.computedStyle.flexDirection)
        assertEquals(JustifyContent.START, node.computedStyle.justifyContent)
        assertEquals(AlignItems.STRETCH, node.computedStyle.alignItems)
        assertEquals(24f, node.computedStyle.gap)
        assertEquals(16f, node.computedStyle.paddingLeft)
        assertEquals(16f, node.computedStyle.paddingRight)
        assertEquals(8f, node.computedStyle.paddingTop)
        assertEquals(8f, node.computedStyle.paddingBottom)
        assertEquals(4f, node.computedStyle.marginTop)
        assertEquals(4f, node.computedStyle.marginBottom)
        assertEquals(10f, node.computedStyle.borderRadius)
        assertEquals(18f, node.computedStyle.fontSize)
        assertEquals(0xFF112233.toInt(), node.computedStyle.backgroundColor)
    }
}
