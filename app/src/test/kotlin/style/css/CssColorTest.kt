package com.foam.app.style.css

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class CssColorTest {

    @Test
    fun hexShortHand() {

        // #fff -> 0xFF, 0xFF, 0xFF
        assertEquals(
            0xFF shl 24 or 0xFFFFFF,
            invoke("#fff")
        )

        // #000 -> 0
        assertEquals(
            0xFF shl 24 or 0,
            invoke("#000")
        )

        // #f00 -> red
        assertEquals(
            0xFF shl 24 or 0xFF0000,
            invoke("#f00")
        )
    }


    @Test
    fun hexLongHand() {

        assertEquals(
            0xFF shl 24 or 0x000000,
            invoke("#000000")
        )

        assertEquals(
            0xFF shl 24 or 0xFFFFFF,
            invoke("#ffffff")
        )

        assertEquals(
            0xFF shl 24 or 0x008CBA,
            invoke("#008cba")
        )
    }


    @Test
    fun namedColors() {

        assertEquals(
            0xFF shl 24 or 0x000000,
            invoke("black")
        )

        assertEquals(
            0xFF shl 24 or 0xFFFFFF,
            invoke("white")
        )

        assertEquals(
            0xFF shl 24 or 0x0000FF,
            invoke("blue")
        )

        // Case-insensitive
        assertEquals(
            0xFF shl 24 or 0xFF0000,
            invoke("RED")
        )
    }


    @Test
    fun rgbFunction() {

        assertEquals(
            0xFF shl 24 or 0x000000,
            invoke("rgb(0, 0, 0)")
        )

        assertEquals(
            0xFF shl 24 or 0xFFFFFF,
            invoke("rgb(255, 255, 255)")
        )

        // Single-space form
        assertEquals(
            0xFF shl 24 or 0xFF0000,
            invoke("rgb(255,0,0)")
        )
    }


    @Test
    fun rgbaFunctionWithAlpha() {

        // alpha 1.0 -> opaque red
        assertEquals(
            0xFF shl 24 or 0xFF0000,
            invoke("rgba(255, 0, 0, 1.0)")
        )

        // alpha 0.5 -> 128 == 0x80
        assertEquals(
            0x80 shl 24 or 0x00FF00,
            invoke("rgba(0, 255, 0, 0.5)")
        )
    }


    @Test
    fun rgbPercentage() {

        // 100% red
        assertEquals(
            0xFF shl 24 or 0xFF0000,
            invoke("rgb(100%, 0%, 0%)")
        )

        // 50% gray
        assertEquals(
            0xFF shl 24 or 0x808080.toInt(),
            invoke("rgb(50%, 50%, 50%)")
        )
    }


    @Test
    fun rejectsGarbage() {

        assertFails {
            invoke("not-a-color")
        }

        assertFails {
            invoke("#")
        }

        assertFails {
            invoke("#xyz")
        }
    }


    /**
     * Invoke the private [Css4jStyleEngine.cssColor] via reflection so
     * we can unit-test the parser without going through the full
     * stylesheet pipeline.
     */
    private fun invoke(input: String): Int {

        val method =
            Css4jStyleEngine::class.java
                .getDeclaredMethod("cssColor", String::class.java)

        method.isAccessible = true

        val instance =
            Css4jStyleEngine::class.java
                .getDeclaredConstructor(String::class.java)
                .apply { isAccessible = true }
                .newInstance("/* unused */")

        return method.invoke(instance, input) as Int
    }
}
