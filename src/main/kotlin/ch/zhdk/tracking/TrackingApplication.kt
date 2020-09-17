package ch.zhdk.tracking

import ch.bildspur.math.Float2
import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.osc.OscPublisher
import ch.zhdk.tracking.osc.OscUDPChannel
import ch.zhdk.tracking.osc.OscWebSocketChannel
import ch.zhdk.tracking.pipeline.PassthroughPipeline
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineType
import ch.zhdk.tracking.pipeline.SimpleTrackingPipeline
import ch.zhdk.tracking.web.WebServer
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import java.awt.image.BufferedImage
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

    private var pipeline: Pipeline = PassthroughPipeline(PipelineConfig(), EmptyInputProvider())

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
            drawText("starting pipeline...")
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
                        g.fill = javafx.scene.paint.Color.BLACK
                        g.fillRect(0.0, 0.0, canvas.width, canvas.height)

                        if (backgroundImage != null)
                            drawImage(g, backgroundImage!!)

                        if (config.pipeline.annotateOutput.value) {
                            annotate(g)
                        }
                    }
                } else {
                    Thread.sleep(100)
                }
            }

            if (restartRequested) {
                config.message.value = "restart requested..."
                drawText("restarting...")
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

    private fun drawText(message: String) {
        // todo: implement setting text
    }

    private fun annotate(g: GraphicsContext) {
        // todo: annotate
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
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Passthrough -> PassthroughPipeline(config.pipeline, EmptyInputProvider(), pipelineLock)
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider(), pipelineLock)
        }
    }
}