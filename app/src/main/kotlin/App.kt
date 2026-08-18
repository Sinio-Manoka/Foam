package com.foam.app

import com.foam.app.dsl.components.Button
import com.foam.app.dsl.components.Text
import com.foam.app.runtime.FoamApp

class App : FoamApp(

    config = {
        title = "Foam"
        width = 900f
        height = 600f
        stylesheet = "/styles/app.css"
    },

    export = {
        VStack("container") {
            Button("button", "primary") {
                Text(
                    "Continue",
                    "button-label"
                )

            }
        }
    }
)


fun main() {

    App().launch()
}
