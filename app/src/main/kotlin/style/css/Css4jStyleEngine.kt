package com.foam.app.style.css

import com.foam.app.core.node.Node
import com.foam.app.style.AlignItems
import com.foam.app.style.ComputedStyle
import com.foam.app.style.Display
import com.foam.app.style.FlexDirection
import com.foam.app.style.JustifyContent
import io.sf.carte.doc.dom.CSSDOMImplementation
import io.sf.carte.doc.style.css.CSSStyleSheet
import io.sf.carte.doc.style.css.om.StyleRule
import java.io.StringReader

class Css4jStyleEngine(
    css: String
) {
    private val styleSheet =
        CSSDOMImplementation()
            .createStyleSheet(
                "StyleSheet",
                null
            )
            .apply {
                parseStyleSheet(
                    StringReader(css),
                    CSSStyleSheet.COMMENTS_IGNORE
                )
            }
    fun applyStyles(root: Node) {
        applyNode(root)

        for (chile in root.children) {
            applyStyles(chile)
        }
    }

    fun applyNode(node: Node) {
        val style = ComputedStyle()
        for (className in node.classes) {
            val rule = findRule(".$className")
                ?:continue
            applyRule(
                rule,
                style
            )
        }
        node.computedStyle = style
    }
    private fun applyRule(
        rule: StyleRule,
        style: ComputedStyle
    ){
        val declaration = rule.style
        declaration
            .getPropertyValue("width")
            .takeIf { it.isNotBlank() }
            ?.let { style.width = px(it) }

        declaration.getPropertyValue("display")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.display =
                    when (it.trim()) {
                        "flex" -> Display.FLEX
                        "none" -> Display.NONE
                        else -> Display.BLOCK
                    }
            }

        declaration.getPropertyValue("flex-direction")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.flexDirection =
                    when (it.trim()) {
                        "column" -> FlexDirection.COLUMN
                        else -> FlexDirection.ROW
                    }
            }

        declaration.getPropertyValue("justify-content")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.justifyContent =
                    when (it.trim()) {
                        "center" -> JustifyContent.CENTER
                        "flex-end" -> JustifyContent.END
                        "space-between" -> JustifyContent.SPACE_BETWEEN
                        "space-around" -> JustifyContent.SPACE_AROUND
                        "space-evenly" -> JustifyContent.SPACE_EVENLY
                        else -> JustifyContent.START
                    }
            }

        declaration.getPropertyValue("align-items")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.alignItems =
                    when (it.trim()) {
                        "center" -> AlignItems.CENTER
                        "flex-end" -> AlignItems.END
                        "stretch" -> AlignItems.STRETCH
                        else -> AlignItems.START
                    }
            }

        declaration.getPropertyValue("margin")
            .takeIf { it.isNotBlank() }
            ?.let {
                applyBoxShorthand(
                    it,
                    onTop = { value -> style.marginTop = value },
                    onRight = { value -> style.marginRight = value },
                    onBottom = { value -> style.marginBottom = value },
                    onLeft = { value -> style.marginLeft = value }
                )
            }

        declaration.getPropertyValue("margin-top")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.marginTop = px(it)
            }

        declaration.getPropertyValue("margin-right")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.marginRight = px(it)
            }

        declaration.getPropertyValue("margin-bottom")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.marginBottom = px(it)
            }

        declaration.getPropertyValue("margin-left")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.marginLeft = px(it)
            }


        declaration.getPropertyValue("padding")
            .takeIf { it.isNotBlank() }
            ?.let {
                applyBoxShorthand(
                    it,
                    onTop = { value -> style.paddingTop = value },
                    onRight = { value -> style.paddingRight = value },
                    onBottom = { value -> style.paddingBottom = value },
                    onLeft = { value -> style.paddingLeft = value }
                )
            }

        declaration.getPropertyValue("padding-top")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.paddingTop = px(it)
            }

        declaration.getPropertyValue("padding-right")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.paddingRight = px(it)
            }

        declaration.getPropertyValue("padding-bottom")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.paddingBottom = px(it)
            }

        declaration.getPropertyValue("padding-left")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.paddingLeft = px(it)
            }

        declaration
            .getPropertyValue("height")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.height = px(it)
            }

        declaration
            .getPropertyValue("gap")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.gap = px(it)
            }

        declaration
            .getPropertyValue("border-radius")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.borderRadius = px(it)
            }

        declaration
            .getPropertyValue("font-size")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.fontSize = px(it)
            }

        declaration
            .getPropertyValue("background-color")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.backgroundColor =
                    cssColor(it)
            }

        declaration
            .getPropertyValue("color")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.textColor =
                    cssColor(it)
            }
    }
    private fun findRule(selector: String): StyleRule? {
        val rules = styleSheet.cssRules
        for (i in 0 until rules.length) {
            val rule = rules.item(i)
            if (rule is StyleRule && rule.selectorList.toString().trim() == selector) {
                return rule
            }
        }
        return null
    }

    private fun px(value: String): Float
    {
        return value.trim().removeSuffix("px").toFloat()
    }

    private fun applyBoxShorthand(
        value: String,
        onTop: (Float) -> Unit,
        onRight: (Float) -> Unit,
        onBottom: (Float) -> Unit,
        onLeft: (Float) -> Unit
    ) {

        val values =
            value.trim()
                .split(Regex("\\s+"))
                .map { px(it) }

        when (values.size) {

            1 -> {
                val all = values[0]

                onTop(all)
                onRight(all)
                onBottom(all)
                onLeft(all)
            }

            2 -> {
                val vertical = values[0]
                val horizontal = values[1]

                onTop(vertical)
                onBottom(vertical)

                onRight(horizontal)
                onLeft(horizontal)
            }

            3 -> {
                val top = values[0]
                val horizontal = values[1]
                val bottom = values[2]

                onTop(top)

                onRight(horizontal)
                onLeft(horizontal)

                onBottom(bottom)
            }

            4 -> {
                onTop(values[0])
                onRight(values[1])
                onBottom(values[2])
                onLeft(values[3])
            }

            else ->
                error(
                    "Unsupported CSS box shorthand: $value"
                )
        }
    }

    private fun cssColor(value: String): Int {

        val hex =
            value
                .trim()
                .removePrefix("#")

        return when (hex.length) {

            3 -> {
                val r = hex[0].digitToInt(16) * 17
                val g = hex[1].digitToInt(16) * 17
                val b = hex[2].digitToInt(16) * 17

                (0xFF shl 24) or
                        (r shl 16) or
                        (g shl 8) or
                        b
            }

            6 -> {
                0xFF000000.toInt() or
                        hex.toInt(16)
            }

            else ->
                error(
                    "Unsupported CSS color: $value"
                )
        }
    }
}
