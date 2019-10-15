package ch.zhdk.tracking

import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.osc.OscPublisher
import ch.zhdk.tracking.pipeline.PassthroughPipeline
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineType
import ch.zhdk.tracking.pipeline.SimpleTrackingPipeline
import org.bytedeco.javacv.CanvasFrame
import java.net.InetAddress
import java.nio.file.Paths
import kotlin.math.roundToLong
import kotlin.system.exitProcess
import javax.imageio.ImageIO
import java.io.File


object CVPreview {
    lateinit var config: AppConfig

    @Volatile
    var running = true

    @Volatile
    var restartRequested = false

    @Volatile
    var saveFrameRequested = false

    private val pipelineLock = Any()

    private val osc = OscPublisher()
    private val oscTimer = ElapsedTimer()

    fun start(config: AppConfig) {
        this.config = config
        val canvasFrame = CanvasFrame("Preview")
        canvasFrame.setCanvasSize(1280, 720)

        setupConfigChangedHandlers()
        initOSC()

        while (running) {
            config.message.value = "starting pipeline..."

            // pipeline init
            var pipeline = createPipeline()

            pipeline.onFrameProcessed += {
                if (oscTimer.elapsed()) {
                    osc.publish(pipeline.tactileObjects)
                }
            }

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
                if(pipeline.isRunning) {
                    synchronized(pipelineLock) {
                        if (config.displayProcessed.value) {
                            canvasFrame.showImage(pipeline.processedFrame)
                        } else {
                            canvasFrame.showImage(pipeline.inputFrame)
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

    private fun setupConfigChangedHandlers() {
        config.osc.updateFrequency.onChanged += {
            oscTimer.duration = (1000.0 / config.osc.updateFrequency.value).roundToLong()
        }
        config.osc.updateFrequency.fireLatest()
    }

    fun initOSC() {
        osc.init(InetAddress.getByName(config.osc.oscAddress.value), config.osc.oscPort.value)
    }

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
                config.input.realSenseFrameRate.value
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