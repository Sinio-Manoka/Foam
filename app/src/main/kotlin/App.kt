package com.foam.app

import com.foam.app.dsl.components.Text
import com.foam.app.dsl.components.VStack
import com.foam.app.runtime.FoamApp

/**
 * Foam demo:
 *
 * A black stage with two large rounded boxes side-by-side. The left one
 * is blue with text aligned to the top-left corner. The right one is
 * purple with text aligned to the bottom-right corner. Layout (centering,
 * gap, flex direction) and per-corner text alignment both come from the
 * stylesheet.
 */
class App : FoamApp(

    config = {

        title = "Foam"
        width = 900f
        height = 600f
        stylesheet = "/styles/app.css"
    },

    export = {

        VStack("stage") {

            VStack("box", "box-a") {

                Text(
                    "Hello",
                    "box-label"
                )

                Text(
                    "from the top-left corner",
                    "box-caption"
                )
            }

            VStack("box", "box-b") {

                Text(
                    "World",
                    "box-label"
                )

                Text(
                    "from the bottom-right corner",
                    "box-caption"
                )
            }
        }
    }
)


fun main() {

    App().launch()
}
