package com.foam.app.layout.taffy

import com.foam.app.core.node.ElementNode
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

/**
 * Diagnostic test that prints the actual layout values for the demo
 * scenario. Used to inspect where the text leaf lands inside its
 * button.
 */
internal class LayoutDebugTest {

    private class StdoutTextEngine : TextEngine {
        override fun measure(node: TextNode, maxWidth: Float, scale: Float): TextLayout {
            // Approximate Segoe UI 20 px at scale 1.
            val heightPx = 24f
            val widthPx = maxWidth.coerceAtMost(80f)
            println(
                "[measure] text='${node.text}' maxWidth=$maxWidth " +
                    "→ height=${heightPx}, width=$widthPx"
            )
            return object : TextLayout {
                override val width: Float = widthPx
                override val height: Float = heightPx
                override val baseline: Float = 19f
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


    @Test
    fun debugTwoButtonsLayout() {

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

        val labelA = TextNode("Continue").also { it.classes += "button-label" }
        val labelB = TextNode("\u064A\u0643\u0645\u0644").also { it.classes += "arabic" }

        stage.add(buttonA); buttonA.add(labelA)
        stage.add(buttonB); buttonB.add(labelB)

        TaffyLayoutEngine(StdoutTextEngine()).layout(
            root = stage,
            width = 900f,
            height = 600f,
            scale = 1f
        )

        println("\n===== FINAL LAYOUTS =====")
        println(
            "stage: x=${stage.layout.x} y=${stage.layout.y} " +
                "w=${stage.layout.width} h=${stage.layout.height}"
        )
        for (btn in listOf(buttonA, buttonB)) {
            println(
                "${btn.type}: x=${btn.layout.x} y=${btn.layout.y} " +
                    "w=${btn.layout.width} h=${btn.layout.height}"
            )
            for (child in btn.children) {
                val t = child as TextNode
                println(
                    "  text '${t.text}': x=${t.layout.x} y=${t.layout.y} " +
                        "w=${t.layout.width} h=${t.layout.height}"
                )
            }
        }
    }
}
