package com.foam.app

import com.foam.app.core.node.ElementNode
import com.foam.app.core.node.TextNode
import com.foam.app.layout.taffy.TaffyLayoutEngine
import com.foam.app.style.css.Css4jStyleEngine
import org.jetbrains.desktop.win32.Application
import org.jetbrains.desktop.win32.Event
import org.jetbrains.desktop.win32.EventHandlerResult
import org.jetbrains.desktop.win32.KotlinDesktopToolkit
import org.jetbrains.desktop.win32.LogLevel
import org.jetbrains.desktop.win32.LogicalSize
import org.jetbrains.desktop.win32.WindowParams
import org.jetbrains.desktop.win32.AngleRenderer
import org.jetbrains.desktop.win32.PhysicalSize
import org.jetbrains.desktop.win32.SurfaceParams
import com.foam.app.render.skia.SkiaRenderer
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.paragraph.Alignment

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.GLAssembledInterface
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.makeGLWithInterface

/*
 * CSS
 */
val css = """
    .window {
        width: 900px;
        height: 600px;
    }

    .label {
        font-size: 16px;
        color: #ffffff;
    }

    .container {
        gap: 24px;
    }

    .box {
        width: 180px;
        height: 180px;
        border-radius: 18px;
    }

    .first {
        background-color: #007aff;
    }

    .second {
        background-color: #af52de;
    }
""".trimIndent()




val label =
    TextNode("Hello Foam مرحبا").apply {
        classes += "label"
    }

val firstBox =
    ElementNode("box").apply {
        classes += "box"
        classes += "first"
        add(label)
    }
val secondBox =
    ElementNode("box").apply {
        classes += "box"
        classes += "second"
    }
val root =
    ElementNode("container").apply {
        classes += "container"
        add(firstBox)
        add(secondBox)
    }
val styleEngine =
    Css4jStyleEngine(css).apply {
        applyStyles(root)
    }
val layoutEngine =
    TaffyLayoutEngine()

val renderEngine =
    SkiaRenderer()

class FoamWindow(
    private val app: Application
) : AutoCloseable {

    val window =
        app.newWindow()

    private val renderer: AngleRenderer by lazy {
        app.createAngleRenderer(window)
    }

    private val directContext: DirectContext by lazy {

        val egl =
            renderer.getEglGetProcFunc()

        val glInterface =
            GLAssembledInterface.createFromNativePointers(
                ctxPtr = egl.ctxPtr,
                fPtr = egl.fPtr
            )

        DirectContext.makeGLWithInterface(
            glInterface
        )
    }

    private var currentSize =
        PhysicalSize(0, 0)

    private var surfaceParams:
            SurfaceParams? = null


    fun handleEvent(
        event: Event
    ): EventHandlerResult {

        return when (event) {

            is Event.WindowDraw -> {
                drawFrame(
                    event.size,
                    event.scale
                )

                EventHandlerResult.Stop
            }

            is Event.NCCalcSize -> {
                drawFrame(
                    event.size,
                    event.scale
                )

                EventHandlerResult.Stop
            }

            is Event.WindowResize -> {
                window.requestRedraw()
                EventHandlerResult.Continue
            }

            is Event.WindowScaleChanged -> {
                window.requestRedraw()
                EventHandlerResult.Continue
            }

            else ->
                EventHandlerResult.Continue
        }
    }


    private fun drawFrame(
        size: PhysicalSize,
        scale: Float
    ) {

        renderer.makeCurrent()

        if (
            currentSize.width != size.width ||
            currentSize.height != size.height
        ) {

            surfaceParams =
                renderer.resizeSurface(
                    size.width,
                    size.height
                )

            currentSize = size
        }

        val params =
            surfaceParams ?: return

        BackendRenderTarget.makeGL(
            width = size.width,
            height = size.height,
            sampleCnt = 1,
            stencilBits = 8,
            fbId = params.framebufferBinding,
            fbFormat = FramebufferFormat.GR_GL_RGBA8
        ).use { renderTarget ->

            Surface.makeFromBackendRenderTarget(
                context = directContext,
                rt = renderTarget,
                origin = SurfaceOrigin.BOTTOM_LEFT,
                colorFormat = SurfaceColorFormat.RGBA_8888,
                colorSpace = ColorSpace.sRGB,
                surfaceProps = null
            )!!.use { surface ->

                drawUI(
                    surface.canvas,
                    size,
                    scale
                )

                surface.flushAndSubmit()

                renderer.swapBuffers()
            }
        }
    }


    private fun drawUI(
        canvas: Canvas,
        windowSize: PhysicalSize,
        scale: Float
    ) {

        val logicalWidth =
            windowSize.width / scale

        val logicalHeight =
            windowSize.height / scale

        canvas.clear(
            Color.makeRGB(
                245,
                245,
                247
            )
        )

        layoutEngine.layout(
            root,
            logicalWidth,
            logicalHeight
        )

        val labelStyle =
            label.computedStyle

        /*
         * SKIA
         */
        renderEngine.render(
            canvas,
            root,
            scale
        )
        /*
         * SKPARAGRAPH
         */
        val fontCollection =
            FontCollection().apply {
                setDefaultFontManager(
                    FontMgr.default
                )
            }

        val textStyle =
            org.jetbrains.skia.paragraph.TextStyle().apply {

                fontFamilies =
                    arrayOf("Segoe UI")

                fontSize =
                    labelStyle.fontSize * scale

                color =
                    labelStyle.textColor
            }

        val paragraphStyle =
            ParagraphStyle().apply {

                this.textStyle =
                    textStyle

                alignment =
                    Alignment.CENTER
            }

        val paragraph =
            ParagraphBuilder(
                paragraphStyle,
                fontCollection
            ).run {

                addText(
                    label.text
                )

                build()
            }

        paragraph.layout(
            firstBox.layout.width * scale
        )

        val textX =
            firstBox.layout.x * scale

        val textY =
            firstBox.layout.y * scale +
                    (
                            firstBox.layout.height * scale -
                                    paragraph.height
                            ) / 2f

        paragraph.paint(
            canvas,
            textX,
            textY
        )
    }


    override fun close() {

        window.destroy()

        window.close()

        directContext.close()
    }
}


fun main() {

    /*
     * WINDOW SIZE
     *
     * Temporary:
     * still using fixed values here.
     *
     * Later Window becomes a real Foam node too.
     */
    val windowWidth =
        900f

    val windowHeight =
        600f

    KotlinDesktopToolkit.init(
        consoleLogLevel =
            LogLevel.Debug
    )

    Application().use { app ->

        lateinit var foamWindow:
                FoamWindow

        app.onStartup {

            foamWindow =
                FoamWindow(app)

            foamWindow.window.create(
                WindowParams(
                    title =
                        "Foam Architecture Demo",

                    size =
                        LogicalSize(
                            width =
                                windowWidth,

                            height =
                                windowHeight
                        )
                )
            )

            foamWindow.window.show()

            foamWindow.window.forceFocus()
        }

        app.runEventLoop {
                _,
                event ->

            when (event) {

                is Event.WindowCloseRequest -> {

                    foamWindow.close()

                    app.stopEventLoop()

                    EventHandlerResult.Stop
                }

                else ->
                    foamWindow
                        .handleEvent(event)
            }
        }
    }
}
