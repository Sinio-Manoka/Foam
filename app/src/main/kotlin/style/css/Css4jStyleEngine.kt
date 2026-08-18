package com.foam.app.style.css

import com.foam.app.core.node.Node
import com.foam.app.style.ComputedStyle
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
