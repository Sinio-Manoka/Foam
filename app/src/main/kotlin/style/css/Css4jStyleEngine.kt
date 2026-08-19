package com.foam.app.style.css

import com.foam.app.core.node.Node
import com.foam.app.style.ComputedStyle
import com.foam.app.style.flex.AlignItems
import com.foam.app.style.flex.Display
import com.foam.app.style.flex.FlexDirection
import com.foam.app.style.flex.JustifyContent
import com.foam.app.style.text.Direction
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
        style.mergeInline(node.inlineStyle)
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

        declaration
            .getPropertyValue("direction")
            .takeIf { it.isNotBlank() }
            ?.let {
                style.direction =
                    when (it.trim()) {
                        "rtl" -> Direction.RTL
                        else -> Direction.LTR
                    }
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

        val trimmed =
            value.trim()

        // Named colors (black, white, red, transparent, ...).
        val named =
            NAMED_COLORS[trimmed.lowercase()]
        if (named != null) {
            // The map stores 24-bit RGB. Apply opaque alpha so the value
            // is in the same ARGB format the renderer expects.
            return 0xFF000000.toInt() or named
        }

        // #rgb / #rrggbb / #rrggbbaa
        if (trimmed.startsWith("#")) {

            val hex =
                trimmed.removePrefix("#")

            return when (hex.length) {

                3 -> {
                    val r = hex[0].digitToInt(16) * 17
                    val g = hex[1].digitToInt(16) * 17
                    val b = hex[2].digitToInt(16) * 17

                    argb(0xFF, r, g, b)
                }

                6 ->
                    argb(0xFF, rgbFromHex(hex))

                8 -> {

                    val a = hex.substring(0, 2).toInt(16)
                    val rgb = rgbFromHex(hex.substring(2, 8))

                    argb(a, rgb)
                }

                else ->
                    error("Unsupported CSS color: $value")
            }
        }

        // rgb(r, g, b) and rgba(r, g, b, a). Numbers may be 0-255 integers
        // or 0.0-1.0 floats.
        val rgbMatch =
            RGB_PATTERN.matchEntire(trimmed.lowercase())
        if (rgbMatch != null) {

            val (r, g, b, a) = rgbMatch.destructured
            return argb(
                a = parseAlpha(a),
                r = parseChannel(r),
                g = parseChannel(g),
                b = parseChannel(b)
            )
        }

        error("Unsupported CSS color: $value")
    }


    /** Pack a 32-bit ARGB int from the four channels. */
    private fun argb(
        a: Int,
        r: Int,
        g: Int,
        b: Int
    ): Int =
        (a and 0xFF shl 24) or
                (r and 0xFF shl 16) or
                (g and 0xFF shl 8) or
                (b and 0xFF)


    /** Pack an ARGB int given a 0xRRGGBB int. */
    private fun argb(
        a: Int,
        rgb: Int
    ): Int =
        (a and 0xFF shl 24) or (rgb and 0xFFFFFF)


    /**
     * Round a normalized 0..255 float to the nearest int, clamping.
     */
    private fun roundToByte(
        f: Float
    ): Int {

        val clamped =
            f.coerceIn(0f, 255f)

        return (clamped + 0.5f).toInt()
            .coerceIn(0, 255)
    }


    /**
     * Parse an RGB-channel token. Accepts either an integer 0-255, a
     * percentage `0%..100%`, or a float 0.0-1.0.
     */
    private fun parseChannel(value: String): Int {

        val s =
            value.trim()

        return when {

            s.endsWith("%") -> {

                val pct =
                    s.removeSuffix("%").trim().toFloat()

                roundToByte(
                    pct.coerceIn(0f, 100f) / 100f * 255f
                )
            }

            '.' in s -> {

                val f =
                    s.toFloat()

                roundToByte(
                    f.coerceIn(0f, 1f) * 255f
                )
            }

            else ->
                s.toInt().coerceIn(0, 255)
        }
    }


    /**
     * Parse an alpha-channel token. CSS `<alpha-value>` is a `<number>`
     * (float 0.0-1.0) or `<percentage>`. An empty token (the alpha was
     * omitted) defaults to fully opaque (1.0).
     */
    private fun parseAlpha(value: String): Int {

        val s =
            value.trim().ifEmpty { "1" }

        return when {

            s.endsWith("%") -> {

                val pct =
                    s.removeSuffix("%").trim().toFloat()

                roundToByte(
                    pct.coerceIn(0f, 100f) / 100f * 255f
                )
            }

            '.' in s -> {

                val f =
                    s.toFloat()

                roundToByte(
                    f.coerceIn(0f, 1f) * 255f
                )
            }

            else -> {

                // Without a decimal point, CSS treats the value as
                // float 0.0-1.0 (per the `<number>` production). `1`
                // means fully opaque.
                val f =
                    s.toFloat()

                roundToByte(
                    f.coerceIn(0f, 1f) * 255f
                )
            }
        }
    }


    private fun rgbFromHex(hex: String): Int {

        require(hex.length == 6) {
            "Expected 6 hex digits, got '$hex'"
        }

        return hex.toInt(16)
    }


    private val RGB_PATTERN =
        Regex("""rgba?\(\s*([0-9.%]+)\s*,\s*([0-9.%]+)\s*,\s*([0-9.%]+)(?:\s*,\s*([0-9.%]+))?\s*\)""")


    /**
     * The CSS Color Module Level 4 named colors. The full set is 147
     * entries; we ship the most common ones plus `transparent`. Each
     * value is a 24-bit RGB int (no alpha).
     */
    private val NAMED_COLORS: Map<String, Int> = mapOf(

        // Grays / monochrome
        "black" to 0x000000,
        "silver" to 0xC0C0C0,
        "gray" to 0x808080,
        "grey" to 0x808080,
        "white" to 0xFFFFFF,
        "maroon" to 0x800000,
        "olive" to 0x808000,
        "green" to 0x008000,
        "teal" to 0x008080,
        "navy" to 0x000080,
        "purple" to 0x800080,
        "transparent" to 0x000000,

        // Reds / pinks
        "red" to 0xFF0000,
        "orange" to 0xFFA500,
        "yellow" to 0xFFFF00,
        "greenyellow" to 0xADFF2F,
        "lime" to 0x00FF00,
        "aqua" to 0x00FFFF,
        "cyan" to 0x00FFFF,
        "blue" to 0x0000FF,
        "fuchsia" to 0xFF00FF,
        "magenta" to 0xFF00FF,
        "pink" to 0xFFC0CB,
        "lightpink" to 0xFFB6C1,
        "hotpink" to 0xFF69B4,

        // Sky / blue
        "skyblue" to 0x87CEEB,
        "lightblue" to 0xADD8E6,
        "deepskyblue" to 0x00BFFF,
        "dodgerblue" to 0x1E90FF,
        "cornflowerblue" to 0x6495ED,
        "royalblue" to 0x4169E1,
        "steelblue" to 0x4682B4,

        // Greens
        "lightgreen" to 0x90EE90,
        "darkgreen" to 0x006400,
        "forestgreen" to 0x228B22,
        "seagreen" to 0x2E8B57,
        "mediumseagreen" to 0x3CB371,
        "springgreen" to 0x00FF7F,

        // Browns
        "brown" to 0xA52A2A,
        "sienna" to 0xA0522D,
        "chocolate" to 0xD2691E,
        "saddlebrown" to 0x8B4513,

        // Purple-violet range
        "indigo" to 0x4B0082,
        "violet" to 0xEE82EE,
        "plum" to 0xDDA0DD,
        "orchid" to 0xDA70D6,
        "mediumorchid" to 0xBA55D3,
        "darkviolet" to 0x9400D3,
        "blueviolet" to 0x8A2BE2,
        "mediumpurple" to 0x9370DB,
        "mediumslateblue" to 0x7B68EE,

        // Other common shades
        "gold" to 0xFFD700,
        "khaki" to 0xF0E68C,
        "darkkhaki" to 0xBDB76B,
        "coral" to 0xFF7F50,
        "tomato" to 0xFF6347,
        "salmon" to 0xFA8072,
        "lightsalmon" to 0xFFA07A,
        "wheat" to 0xF5DEB3,
        "tan" to 0xD2B48C,
        "lavender" to 0xE6E6FA,
        "beige" to 0xF5F5DC,
        "azure" to 0xF0FFFF,
        "ivory" to 0xFFFFF0,
        "snow" to 0xFFFAFA,
        "linen" to 0xFAF0E6,
        "mintcream" to 0xF5FFFA,
        "whitesmoke" to 0xF5F5F5,
        "lightgray" to 0xD3D3D3,
        "lightgrey" to 0xD3D3D3,
        "darkgray" to 0xA9A9A9,
        "darkgrey" to 0xA9A9A9,
        "dimgray" to 0x696969,
        "dimgrey" to 0x696969
    )
}
