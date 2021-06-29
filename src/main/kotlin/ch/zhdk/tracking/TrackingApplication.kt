package ch.zhdk.tracking

import ch.bildspur.math.Float2
import ch.bildspur.timer.ElapsedTimer
import ch.bildspur.util.format
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.io.ux.InteractiveInputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.state.TrackingEntityState
import ch.zhdk.tracking.osc.OscPublisher
import ch.zhdk.tracking.osc.OscUDPChannel
import ch.zhdk.tracking.osc.OscWebSocketChannel
import ch.zhdk.tracking.pipeline.*
import ch.zhdk.tracking.ui.strokeCircle
import ch.zhdk.tracking.ui.strokeCross
import ch.zhdk.tracking.ui.strokeX
import ch.zhdk.tracking.web.WebServer
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import java.io.File
import java.net.InetAddress
import java.nio.file.Paths
import java.util.concurrent.Semaphore
import javax.imageio.ImageIO
import kotlin.math.roundToLong
import kotlin.system.exitProcess

object TrackingApplication {
    lateinit var config: AppConfig
    lateinit var canvas: Canvas

    @Volatile
    var running = true

    @Volatile
    var saveFrameRequested = false

    @Volatile
    private var restartRequested = false

    @Volatile
    private var pipelineStartedLatch = Semaphore(0)

    private val pipelineLock = Any()

    private val webServer = WebServer()
    private lateinit var oscUDPChannel: OscUDPChannel
    private lateinit var oscWebSocketChannel: OscWebSocketChannel
    private lateinit var osc: OscPublisher

    private val oscTimer = ElapsedTimer()

    @Volatile
    private var mousePressedLedge = Semaphore(0)
    private var mousePressedPosition = Float2()

    var pipeline: Pipeline = PassthroughPipeline(PipelineConfig(), EmptyInputProvider())

    // todo: refactor this heavy method into smaller parts!
    fun start(config: AppConfig, canvas: Canvas) {
        this.config = config
        this.canvas = canvas

        // setup canvas
        canvas.setOnMouseClicked {
            mousePressedPosition = Float2(it.x.toFloat(), it.y.toFloat())
            mousePressedLedge.release()
        }
        canvas.cursor = Cursor.CROSSHAIR

        // create web and udp channel
        webServer.start(config.output.webSocket)
        oscUDPChannel = OscUDPChannel(config.output.osc)
        oscWebSocketChannel = OscWebSocketChannel(webServer, config.output.osc)

        // create osc publisher
        osc = OscPublisher(config.output.osc)
        osc.channels.add(oscUDPChannel)
        osc.channels.add(oscWebSocketChannel)

        setupConfigChangedHandlers()
        initOSC()

        while (running) {
            config.message.value = "starting pipeline..."

            // pipeline init
            pipeline = createPipeline()
            initPipelineHandlers(pipeline)
            config.pipeline.uniqueMarkerId.value = 0

            // try to start pipeline
            pipeline = try {
                pipeline.start()
                config.message.value = "pipeline started"
                pipeline
            } catch (ex: Exception) {
                println("Error: ${ex.message}")
                println(ex.printStackTrace())
                config.message.value = "error on startup"
                config.errorMessage.value = "${ex.message}"

                PassthroughPipeline(config.pipeline, EmptyInputProvider(), pipelineLock)
            }

            // aspect ratio
            setWindowAspectRatio(config.pipeline.inputWidth.value, config.pipeline.inputHeight.value)

            // indicate start finished
            pipelineStartedLatch.release()

            // run
            while (!restartRequested && running) {

                // if frame save requested
                if (saveFrameRequested) {
                    val outfile = File("image_pipeline.png")
                    ImageIO.write(pipeline.inputFrame, "png", outfile)
                    println("frame ${outfile.name} saved!")
                    saveFrameRequested = false
                }

                // display frames
                if (pipeline.isRunning) {
                    pipeline.waitForNewFrameAvailable()

                    var backgroundImage: Image? = null
                    if (config.pipeline.displayOutput.value) {
                        synchronized(pipelineLock) {
                            backgroundImage = if (config.displayProcessed.value) {
                                SwingFXUtils.toFXImage(pipeline.processedFrame, null)
                            } else {
                                SwingFXUtils.toFXImage(pipeline.inputFrame, null)
                            }
                        }
                    }

                    // start drawing output
                    Platform.runLater {
                        val g = canvas.graphicsContext2D

                        // clear canvas
                        g.fill = Color.BLACK
                        g.fillRect(0.0, 0.0, canvas.width, canvas.height)

                        if (backgroundImage != null)
                            drawImage(g, backgroundImage!!)

                        if (config.pipeline.annotateOutput.value) {
                            synchronized(pipelineLock) {
                                annotate(g, pipeline)
                            }
                        }
                    }
                } else {
                    Thread.sleep(100)
                }
            }

            if (restartRequested) {
                config.message.value = "restart requested..."
            }

            pipeline.stop()
            restartRequested = false
        }

        config.message.value = "ended!"
        running = false

        exitProcess(0)
    }

    private fun setWindowAspectRatio(width: Int, height: Int) {
        // todo: set aspect ratio
    }

    private fun drawImage(g: GraphicsContext, image: Image) {
        // draw images
        g.drawImage(image, 0.0, 0.0, canvas.width, canvas.height)
    }

    private fun annotate(g: GraphicsContext, pipeline: Pipeline) {
        // annotate pipeline output
        annotateActiveRegions(g, pipeline.regions)
        annotateMarkers(g, pipeline.markers)
        annotateTactileDevices(g, pipeline.devices)

        // annotate screen calibration
        if (config.pipeline.calibration.displayAnnotation.value) {
            annotateCalibration(g)
        }
    }

    private fun annotateMarkers(g: GraphicsContext, markers: List<Marker>) {
        // annotate tactile objects
        markers.forEach {
            val color = when (it.state) {
                TrackingEntityState.Detected -> Color.CYAN
                TrackingEntityState.Alive -> Color.GREEN
                TrackingEntityState.Missing -> Color.BLUE
                TrackingEntityState.Dead -> Color.YELLOW
            }

            // todo: check for NAN
            g.stroke = color
            g.lineWidth = 1.0
            g.strokeCircle(it.position.x().normWidth(), it.position.y().normHeight(), 10.0)
        }
    }

    private fun annotateActiveRegions(g: GraphicsContext, regions: List<ActiveRegion>) {
        // annotate active regions
        regions.forEach {
            g.stroke = Color.RED

            // mark region
            g.lineWidth = 1.0
            g.strokeCircle(it.center.x().normWidth(), it.center.y().normHeight(), 30.0)

            // show max distance
            val diameter = config.pipeline.markerMaxDelta.value
            g.lineWidth = 0.75
            g.strokeCircle(it.center.x().normWidth(), it.center.y().normHeight(), diameter)
        }
    }

    private fun annotateTactileDevices(g: GraphicsContext, devices: List<TactileDevice>) {
        devices.forEach {
            val defaultColor = Color.YELLOW
            val identifiedColor = Color.GREEN

            g.stroke = defaultColor
            g.lineWidth = 1.0
            g.strokeCross(it.position.x().normWidth(), it.position.y().normHeight(), 30.0)

            if(config.pipeline.smoothPosition.value) {
                g.stroke = Color.MAGENTA
                g.lineWidth = 1.0
                g.strokeX(it.calibratedPosition.x() * canvas.width, it.calibratedPosition.y() * canvas.height, 25.0)
            }

            g.fill = if (it.identifier > -1) identifiedColor else defaultColor
            g.lineWidth = 0.75
            g.fillText(
                "${it.uniqueId} ${if (it.identifier > -1) it.identifier else ""} (r: ${it.rotation.format(1)})",
                it.position.x().normWidth() + 20.0, it.position.y().normHeight() + 20.0
            )
        }
    }

    private fun annotateCalibration(g: GraphicsContext) {
        // display edges and screen
        val screen = Float2(pipeline.inputFrame.width.toFloat(), pipeline.inputFrame.height.toFloat())

        val tl = screen * config.pipeline.calibration.topLeft.value
        val tr = screen * config.pipeline.calibration.topRight.value
        val br = screen * config.pipeline.calibration.bottomRight.value
        val bl = screen * config.pipeline.calibration.bottomLeft.value

        g.stroke = Color.YELLOW
        g.lineWidth = 1.0
        g.strokeCross(tl.x.toDouble().normWidth(), tl.y.toDouble().normHeight(), 10.0)
        g.strokeCross(br.x.toDouble().normWidth(), br.y.toDouble().normHeight(), 10.0)

        if (config.pipeline.calibration.perspectiveTransform.value) {
            g.strokeCross(tr.x.toDouble().normWidth(), tr.y.toDouble().normHeight(), 10.0)
            g.strokeCross(bl.x.toDouble().normWidth(), bl.y.toDouble().normHeight(), 10.0)
        }

        // draw screen
        if (config.pipeline.calibration.perspectiveTransform.value) {
            g.stroke = Color.GRAY
            g.strokePolygon(
                arrayOf(
                    tl.x.toDouble().normWidth(),
                    tr.x.toDouble().normWidth(),
                    br.x.toDouble().normWidth(),
                    bl.x.toDouble().normWidth()
                ).toDoubleArray(),
                arrayOf(
                    tl.y.toDouble().normHeight(),
                    tr.y.toDouble().normHeight(),
                    br.y.toDouble().normHeight(),
                    bl.y.toDouble().normHeight()
                ).toDoubleArray(),
                4
            )
        } else {
            val size = br - tl
            g.stroke = Color.GRAY
            g.strokeRect(
                tl.x.toDouble().normWidth(),
                tl.y.toDouble().normHeight(),
                size.x.toDouble().normWidth(),
                size.y.toDouble().normHeight()
            )
        }
    }

    fun requestPipelineRestart(blocking: Boolean = false) {
        restartRequested = true
        if (blocking)
            pipelineStartedLatch.acquire()
    }

    // internal setup
    private fun setupConfigChangedHandlers() {
        config.output.osc.updateFrequency.onChanged += {
            oscTimer.duration = (1000.0 / config.output.osc.updateFrequency.value).roundToLong()
        }
        config.output.osc.updateFrequency.fireLatest()

        config.input.displaySecondIRStream.onChanged += {
            if (pipeline.inputProvider is RealSense2InputProvider) {
                (pipeline.inputProvider as RealSense2InputProvider).displaySecondChannel = it
            }
        }

        config.productionMode.onChanged += {
            config.pipeline.displayOutput.value = !it
            config.pipeline.annotateOutput.value = !it
            if (it) {
                config.pipeline.enabled.value = true
            }
        }
        config.productionMode.fireLatest()
    }

    private fun initPipelineHandlers(pipeline: Pipeline) {
        pipeline.onFrameProcessed += {
            if (oscTimer.elapsed()) {
                osc.sendUpdate(pipeline.devices.filter { it.isActive })
            }
        }

        pipeline.onDeviceDetected += {
            println("+ adding object: ${it.uniqueId}")
            osc.newObject(it)
        }

        pipeline.onDeviceRemoved += {
            println("- removing object: ${it.uniqueId}")
            osc.removeObject(it)
        }
    }

    // public methods

    fun initOSC() {
        oscUDPChannel.init(InetAddress.getByName(config.output.osc.oscAddress.value), config.output.osc.oscPort.value)
    }

    fun requestMousePressed(): Float2 {
        mousePressedLedge = Semaphore(0)
        mousePressedLedge.acquire()
        return Float2(
            mousePressedPosition.x / canvas.width.toFloat(),
            mousePressedPosition.y / canvas.height.toFloat()
        )
    }

    // factory methods

    private fun createInputProvider(): InputProvider {
        return when (config.input.inputProvider.value) {
            InputProviderType.CameraInput -> CameraInputProvider(
                config.input.webCamDeviceIndex.value,
                config.input.webCamWidth.value,
                config.input.webCamHeight.value
            )
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/markertracking.mp4"))
            InputProviderType.RealSense2 -> RealSense2InputProvider(
                config.input.realSenseDeviceIndex.value,
                config.input.realSenseWidth.value,
                config.input.realSenseHeight.value,
                config.input.realSenseFrameRate.value,
                config.input.enableRGBIRStream.value,
                config.input.enableDualIR.value,
                config.input.displaySecondIRStream.value,
                config = config.input
            )
            InputProviderType.Image -> ImageInputProvider(Paths.get("data/image_pipeline_thrs.png"))
            InputProviderType.Interactive -> InteractiveInputProvider(canvas, Paths.get("data/marker.png"))
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Passthrough -> PassthroughPipeline(config.pipeline, EmptyInputProvider(), pipelineLock)
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider(), pipelineLock)
            PipelineType.Color -> ColorTrackingPipeline(config.pipeline, createInputProvider(), pipelineLock)
        }
    }

    private fun Double.normWidth(): Double {
        return this / pipeline.inputFrame.width * canvas.width
    }

    private fun Double.normHeight(): Double {
        return this / pipeline.inputFrame.height * canvas.height
    }
}