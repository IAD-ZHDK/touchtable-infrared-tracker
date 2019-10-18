package ch.zhdk.tracking

import ch.bildspur.model.math.Float2
import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.osc.OscPublisher
import ch.zhdk.tracking.pipeline.PassthroughPipeline
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineType
import ch.zhdk.tracking.pipeline.SimpleTrackingPipeline
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
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO
import kotlin.math.roundToLong
import kotlin.system.exitProcess


object CVPreview {
    lateinit var config: AppConfig

    @Volatile
    var running = true

    @Volatile
    var saveFrameRequested = false

    @Volatile
    private var restartRequested = false

    @Volatile
    private var pipelineStartedLatch = CountDownLatch(1)

    private val pipelineLock = Any()

    private lateinit var osc: OscPublisher
    private val oscTimer = ElapsedTimer()

    val canvasFrame = CanvasFrame("Preview", 0.0)

    @Volatile
    private var mousePressedLedge = CountDownLatch(1)
    private var mousePressedPosition = Float2()

    private var pipeline : Pipeline = PassthroughPipeline(PipelineConfig(), EmptyInputProvider())

    fun start(config: AppConfig) {
        this.config = config
        setupCanvas()

        osc = OscPublisher(config.osc)

        setupConfigChangedHandlers()
        initOSC()

        while (running) {
            config.message.value = "starting pipeline..."

            // pipeline init
            pipeline = createPipeline()
            initPipelineHandlers(pipeline)

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

            pipelineStartedLatch.countDown()

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
                    synchronized(pipelineLock) {
                        if (config.displayProcessed.value) {
                            drawImage(pipeline.processedFrame, pipeline.annotationFrame)
                        } else {
                            drawImage(pipeline.inputFrame, pipeline.annotationFrame)
                        }
                    }
                }

                // ui refresh speed
                Thread.sleep(1000L / config.updateFrequency.value)
            }

            if (!canvasFrame.isVisible) {
                config.message.value = "shutting down..."
                running = false
            }

            if (restartRequested)
                config.message.value = "restart requested..."

            pipeline.stop()
            restartRequested = false
        }

        config.message.value = "ended!"
        canvasFrame.dispose()
        running = false

        exitProcess(0)
    }

    private fun drawImage(image : BufferedImage, overlay : BufferedImage) {
        val g = canvasFrame.createGraphics()

        // clear canvas
        g.color = Color.black
        g.fillRect(0, 0, canvasFrame.canvas.width, canvasFrame.canvas.height)

        // draw images
        g.drawImage(image, 0, 0, canvasFrame.canvas.width, canvasFrame.canvas.height, null)

        if(config.pipeline.annotateOutput.value) {
            g.composite = BlendComposite.Screen
            g.drawImage(overlay, 0, 0, canvasFrame.canvas.width, canvasFrame.canvas.height, null)
        }

        // paint
        canvasFrame.releaseGraphics(g)
    }

    fun requestPipelineRestart(blocking: Boolean = false) {
        pipelineStartedLatch = CountDownLatch(1)
        restartRequested = true
        if(blocking)
            pipelineStartedLatch.await()
    }

    // internal setup

    private fun setupCanvas() {
        canvasFrame.setCanvasSize(1280, 720)

        // setup mouse listener
        canvasFrame.canvas.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
        canvasFrame.canvas.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                mousePressedPosition = Float2(e.x.toFloat(), e.y.toFloat())
                mousePressedLedge.countDown()
            }
        })
    }

    private fun setupConfigChangedHandlers() {
        config.osc.updateFrequency.onChanged += {
            oscTimer.duration = (1000.0 / config.osc.updateFrequency.value).roundToLong()
        }
        config.osc.updateFrequency.fireLatest()

        config.input.displaySecondIRStream.onChanged += {
            if(pipeline.inputProvider is RealSense2InputProvider) {
                (pipeline.inputProvider as RealSense2InputProvider).displaySecondChannel = it
            }
        }
    }

    private fun initPipelineHandlers(pipeline: Pipeline) {
        pipeline.onFrameProcessed += {
            if (oscTimer.elapsed()) {
                osc.publish(pipeline.tactileObjects)
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
        mousePressedLedge = CountDownLatch(1)
        mousePressedLedge.await()
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
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/irMovieSample.mov"))
            InputProviderType.RealSense2 -> RealSense2InputProvider(
                config.input.realSenseDeviceIndex.value,
                config.input.realSenseWidth.value,
                config.input.realSenseHeight.value,
                config.input.realSenseFrameRate.value,
                config.input.enableRGBIRStream.value,
                config.input.enableDualIR.value,
                config.input.displaySecondIRStream.value
            )
            InputProviderType.Image -> ImageInputProvider(Paths.get("data/image_1512.png"))
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Passthrough -> PassthroughPipeline(config.pipeline, EmptyInputProvider(), pipelineLock)
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider(), pipelineLock)
        }
    }
}