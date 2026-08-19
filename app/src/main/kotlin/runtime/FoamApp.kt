package com.foam.app.runtime

import com.foam.app.core.node.Node
import com.foam.app.dsl.ExportScope
import com.foam.app.layout.taffy.TaffyLayoutEngine
import com.foam.app.render.skia.SkiaRenderer
import com.foam.app.style.css.Css4jStyleEngine
import com.foam.app.text.skia.SkiaTextEngine
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

open class FoamApp(
    config: FoamConfig.() -> Unit = {},
    export: ExportScope.() -> Node = error("FoamApp requires an export { ... } block")
) {

    private var configBlock:
            (FoamConfig.() -> Unit) =
        config

    private var exportBlock:
            (ExportScope.() -> Node) =
        export


    internal fun buildConfig(): FoamConfig {

        val config =
            FoamConfig()

        configBlock
            .invoke(config)

        return config
    }


    internal fun buildUI(): Node {

        return ExportScope()
            .run(exportBlock)
    }


    fun launch() {

        val config =
            buildConfig()

        val css =
            object {}.javaClass
                .getResource(config.stylesheet)
                ?.readText()
                ?: error(
                    "Could not load ${config.stylesheet}"
                )

        val root =
            buildUI()

        val styleEngine =
            Css4jStyleEngine(css)

        val textEngine =
            SkiaTextEngine()

        val layoutEngine =
            TaffyLayoutEngine(
                textEngine
            )

        val renderEngine =
            SkiaRenderer(
                textEngine
            )

        val runtime =
            FoamRuntime(
                styleEngine,
                layoutEngine,
                renderEngine
            )

        runApp(
            config,
            root,
            runtime
        )
    }


    private fun runApp(
        config: FoamConfig,
        root: Node,
        runtime: FoamRuntime
    ) {

        KotlinDesktopToolkit.init(
            consoleLogLevel =
                LogLevel.Debug
        )

        Application().use { app ->

            lateinit var foamWindow:
                    FoamWindow

            app.onStartup {

                foamWindow =
                    FoamWindow(
                        app,
                        config,
                        root,
                        runtime
                    )

                foamWindow.window.create(
                    WindowParams(
                        title =
                            config.title,

                        size =
                            LogicalSize(
                                width =
                                    config.width,

                                height =
                                    config.height
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


    private inner class FoamWindow(
        private val app: Application,
        private val config: FoamConfig,
        private val root: Node,
        private val runtime: FoamRuntime
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

                val surface =
                    Surface.makeFromBackendRenderTarget(
                        context = directContext,
                        rt = renderTarget,
                        origin = SurfaceOrigin.BOTTOM_LEFT,
                        colorFormat = SurfaceColorFormat.RGBA_8888,
                        colorSpace = ColorSpace.sRGB,
                        surfaceProps = null
                    )

                if (surface != null) {

                    surface.use {

                        drawUI(
                            it.canvas,
                            size,
                            scale
                        )

                        it.flushAndSubmit()
                    }

                } else {

                    // No surface this frame (e.g. resize race). Just swap
                    // the buffers to advance and try again next time.
                }

                renderer.swapBuffers()
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
                Color.WHITE
            )

            runtime.renderFrame(
                canvas,
                root,
                logicalWidth,
                logicalHeight,
                scale
            )
        }


        override fun close() {

            window.destroy()

            window.close()

            directContext.close()
        }
    }
}
