package com.foam.app

import com.foam.app.dsl.components.Button
import com.foam.app.dsl.components.Text
import com.foam.app.dsl.components.VStack
import com.foam.app.runtime.FoamApp

/**
 * Foam demo: two simple buttons centered in the middle of the window,
 * separated by a gap, each with a short label.
 *
 * - First button is blue (`.button .primary`) with English text
 *   ("Continue").
 * - Second button is purple (`.button .secondary`) with Arabic text
 *   ("يكمل", "continue" in Arabic), styled with `.arabic` so it
 *   renders right-to-left and uses a slightly larger font.
 *
 * Layout — centering, gap, flex direction — is driven entirely by
 * the stylesheet (`app.css`).
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

            Button("button", "primary") {

                Text(
                    "Continue",
                    "button-label"
                )
            }

            Button("button", "secondary") {

                Text(
                    "\u064A\u0643\u0645\u0644",
                    "arabic"
                )
            }
        }
    }
)


fun main() {

    App().launch()
}
