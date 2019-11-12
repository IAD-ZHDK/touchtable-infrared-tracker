package ch.zhdk.tracking

import ch.bildspur.model.math.Float2
import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.osc.OscPublisher
import ch.zhdk.tracking.pipeline.*
import org.bytedeco.javacv.CanvasFrame
import org.guy.composite.BlendComposite
import java.awt.Color
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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

    @Volatile
    var running = true

    @Volatile
    var saveFrameRequested = false

    @Volatile
    private var restartRequested = false

    @Volatile
    private var pipelineStartedLatch = Semaphore(0)

    private val pipelineLock = Any()

    private lateinit var osc: OscPublisher
    private val oscTimer = ElapsedTimer()

    private val canvasFrame = CanvasFrame("Preview", 0.0)

    @Volatile
    private var mousePressedLedge = Semaphore(0)
    private var mousePressedPosition = Float2()

    private var pipeline: Pipeline = PassthroughPipeline(PipelineConfig(), EmptyInputProvider())

    // todo: refactor this heavy method into smaller parts!
    fun start(config: AppConfig) {
        this.config = config
        setupCanvas()

        osc = OscPublisher(config.osc)

        setupConfigChangedHandlers()
        initOSC()

        while (running) {
            drawText("starting pipeline...")
            config.message.value = "starting pipeline..."

            // pipeline init
            pipeline = createPipeline()
            initPipelineHandlers(pipeline)
            config.pipeline.uniqueId.value = 0

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
            while (!restartRequested && running && canvasFrame.isVisible) {

                // if frame save requested
                if (saveFrameRequested) {
                    val outfile = File("image_pipeline.png")
                    ImageIO.write(pipeline.inputFrame, "png", outfile)
                    println("frame ${outfile.name} saved!")
                    saveFrameRequested = false
                }

                // display frames
                if (pipeline.isRunning) {
                    if (config.pipeline.displayOutput.value) {
                        pipeline.waitForNewFrameAvailable()

                        synchronized(pipelineLock) {
                            if (config.displayProcessed.value) {
                                drawImage(pipeline.processedFrame, pipeline.annotationFrame)
                            } else {
                                drawImage(pipeline.inputFrame, pipeline.annotationFrame)
                            }
                        }
                    } else {
                        Thread.sleep(100)
                    }
                }
            }

            if (!canvasFrame.isVisible) {
                config.message.value = "shutting down..."
                running = false
            }

            if (restartRequested) {
                config.message.value = "restart requested..."
                drawText("restarting...")
            }

            pipeline.stop()
            restartRequested = false
        }

        config.message.value = "ended!"
        canvasFrame.dispose()
        running = false

        exitProcess(0)
    }

    private fun drawImage(image: BufferedImage, overlay: BufferedImage) {
        val g = canvasFrame.createGraphics()

        // clear canvas
        g.color = Color.black
        g.fillRect(0, 0, canvasFrame.canvas.width, canvasFrame.canvas.height)

        // draw images
        g.drawImage(image, 0, 0, canvasFrame.canvas.width - 1, canvasFrame.canvas.height - 1, null)

        if (config.pipeline.annotateOutput.value) {
            g.composite = BlendComposite.Screen

            try {
                g.drawImage(overlay, 0, 0, canvasFrame.canvas.width - 1, canvasFrame.canvas.height - 1, null)
            } catch (ex: Exception) {
                println(ex.message)
            }
        }

        // paint
        canvasFrame.releaseGraphics(g)
    }

    private fun drawText(message: String) {
        val g = canvasFrame.createGraphics()

        // clear canvas
        g.color = Color.DARK_GRAY
        g.fillRect(0, 0, canvasFrame.canvas.width, canvasFrame.canvas.height)

        g.font = g.font.deriveFont(30f)
        g.color = Color.WHITE

        val metrics = g.getFontMetrics(g.font)
        g.drawString(
            message,
            (canvasFrame.canvas.width / 2f) - (metrics.stringWidth(message) / 2f),
            (canvasFrame.canvas.height / 2f) - (metrics.height / 2f)
        )

        // paint
        canvasFrame.releaseGraphics(g)
    }

    fun requestPipelineRestart(blocking: Boolean = false) {
        restartRequested = true
        if (blocking)
            pipelineStartedLatch.acquire()
    }

    // internal setup

    private fun setupCanvas() {
        canvasFrame.setCanvasSize(config.previewWidth.value, config.previewHeight.value)

        // setup mouse listener
        canvasFrame.canvas.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
        canvasFrame.canvas.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                mousePressedPosition = Float2(e.x.toFloat(), e.y.toFloat())
                mousePressedLedge.release()
            }
        })
    }

    private fun setWindowAspectRatio(inputWidth: Int, inputHeight: Int) {
        // use width as constant
        // todo: calculate aspect ratio
    }

    private fun setupConfigChangedHandlers() {
        config.osc.updateFrequency.onChanged += {
            oscTimer.duration = (1000.0 / config.osc.updateFrequency.value).roundToLong()
        }
        config.osc.updateFrequency.fireLatest()

        config.input.displaySecondIRStream.onChanged += {
            if (pipeline.inputProvider is RealSense2InputProvider) {
                (pipeline.inputProvider as RealSense2InputProvider).displaySecondChannel = it
            }
        }
    }

    private fun initPipelineHandlers(pipeline: Pipeline) {
        pipeline.onFrameProcessed += {
            if (oscTimer.elapsed()) {
                osc.sendUpdate(pipeline.markers.filter { it.isActive })
            }
        }

        pipeline.onObjectDetected += {
            println("+ adding object: ${it.uniqueId}")
            osc.newObject(it)
        }

        pipeline.onObjectRemoved += {
            println("- removing object: ${it.uniqueId}")
            osc.removeObject(it)
        }
    }

    // public methods

    fun initOSC() {
        osc.init(InetAddress.getByName(config.osc.oscAddress.value), config.osc.oscPort.value)
    }

    fun requestMousePressed(): Float2 {
        mousePressedLedge.acquire()
        return Float2(
            mousePressedPosition.x / canvasFrame.canvasSize.width,
            mousePressedPosition.y / canvasFrame.canvasSize.height
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
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/markertracking.mov"))
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
            InputProviderType.Image -> ImageInputProvider(Paths.get("data/image_pipeline_3.png"))
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Passthrough -> PassthroughPipeline(config.pipeline, EmptyInputProvider(), pipelineLock)
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider(), pipelineLock)
            PipelineType.RGBIR -> RGBIRPipeline(config.pipeline, createInputProvider(), pipelineLock)
        }
    }
}