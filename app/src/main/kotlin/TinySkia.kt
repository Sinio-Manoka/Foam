package com.foam.app

import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface

fun main() {
    val surface = Surface.makeRasterN32Premul(800, 600)
    val canvas = surface.canvas
    canvas.clear(Color.WHITE)
    val paint = Paint().apply {
        color = Color.makeRGB(
            0,
            122,
            255
        )
    }
    canvas.drawRect(
        100f,
        100f,
        400f,
        220f,
        paint
    )

    print("Done")
    paint.close()
    surface.close()
}
