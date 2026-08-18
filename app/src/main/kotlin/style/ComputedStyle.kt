package com.foam.app.style

data class ComputedStyle(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var gap: Float = 0f,
    var backgroundColor : Int? = null,
    var fontSize: Float = 0f,
    var textColor : Int = 0xFF000000.toInt(),
    var borderRadius: Float = 0f
)
