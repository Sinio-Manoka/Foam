package com.foam.app.core.node

import com.foam.app.text.TextLayout

open class TextNode(
    val text: String
) : Node() {

    var textLayout: TextLayout? = null
}
