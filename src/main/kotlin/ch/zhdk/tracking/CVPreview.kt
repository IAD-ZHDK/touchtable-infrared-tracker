package ch.zhdk.tracking

import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.javacv.toBufferedImage
import ch.zhdk.tracking.javacv.toIplImage
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

    private val osc = OscPublisher()
    private val oscTimer = ElapsedTimer()

    fun start(config: AppConfig) {
        this.config = config
        val canvasFrame = CanvasFrame("Preview")
        canvasFrame.setCanvasSize(1280, 720)

        setupConfigChangedHandlers()
        initOSC()

        while(running) {
            // pipeline init
            var pipeline = createPipeline()

            pipeline.onFrameProcessed += {
                if(oscTimer.elapsed()) {
                    osc.publish(pipeline.tactileObjects)
                }
            }

            // try to start pipeline
            pipeline = try {
                pipeline.start()
                config.message.value = "started"
                pipeline
            } catch (ex : Exception) {
                println("Error: ${ex.message}")
                println(ex.printStackTrace())
                config.message.value = "Error: ${ex.message}"

                PassthroughPipeline(config.pipeline, EmptyInputProvider())
            }

            // run
            while (!restartRequested && running && canvasFrame.isVisible) {
                if(saveFrameRequested) {
                    val img = pipeline.inputFrame.toIplImage().toBufferedImage()
                    val outfile = File("image_${pipeline.inputFrame.timestamp}.png")
                    ImageIO.write(img, "png", outfile)
                    println("frame ${outfile.name} saved!")
                    saveFrameRequested = false
                }

                if (config.displayProcessed.value) {
                    canvasFrame.showImage(pipeline.processedFrame)
                } else {
                    canvasFrame.showImage(pipeline.inputFrame)
                }
            }

            if(!canvasFrame.isVisible) {
                config.message.value = "shutting down..."
                running = false
            }

            if(restartRequested)
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
        config.output.updateFrequency.onChanged += {
            oscTimer.duration = (1000.0 / config.output.updateFrequency.value).roundToLong()
        }
        config.output.updateFrequency.fireLatest()
    }

    fun initOSC() {
        osc.init(InetAddress.getByName(config.output.oscAddress.value), config.output.oscPort.value)
    }

    private fun createInputProvider(): InputProvider {
        return when (config.input.inputProvider.value) {
            InputProviderType.CameraInput -> CameraInputProvider(config.input.deviceIndex.value, 1280, 720)
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/irMovieSample.mov"))
            InputProviderType.RealSense2 -> RealSense2InputProvider(config.input.deviceIndex.value, 848, 480, 60)
            InputProviderType.Image -> ImageInputProvider(Paths.get("data/image_1512.png"))
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Passthrough -> PassthroughPipeline(config.pipeline, EmptyInputProvider())
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider())
        }
    }
}