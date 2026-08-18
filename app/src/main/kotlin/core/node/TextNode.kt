package com.foam.app.core.node

import com.foam.app.text.TextLayout

class TextNode(
    val text: String
) : Node() {

    var textLayout: TextLayout? = null
}
