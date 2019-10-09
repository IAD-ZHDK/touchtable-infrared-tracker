package ch.zhdk.tracking

import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.io.*
import ch.zhdk.tracking.osc.OscPublisher
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineType
import ch.zhdk.tracking.pipeline.SimpleTrackingPipeline
import org.bytedeco.javacv.CanvasFrame
import java.net.InetAddress
import java.nio.file.Paths
import javax.swing.WindowConstants
import kotlin.math.roundToLong

object CVPreview {
    lateinit var config: AppConfig

    @Volatile
    var running = true

    @Volatile
    var restartRequested = true

    private val osc = OscPublisher()
    private val oscTimer = ElapsedTimer()

    fun start(config: AppConfig) {
        this.config = config
        val canvasFrame = CanvasFrame("Preview")
        canvasFrame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        canvasFrame.setCanvasSize(1280, 720)

        setupConfigChangedHandlers()
        initOSC()

        while(running) {
            val pipeline = createPipeline()
            pipeline.onFrameProcessed += {
                if(oscTimer.elapsed()) {
                    osc.publish(pipeline.tactileObjects)
                }
            }

            pipeline.start()

            while (!restartRequested && running && canvasFrame.isVisible) {
                if (config.displayProcessed.value) {
                    canvasFrame.showImage(pipeline.processedFrame)
                } else {
                    canvasFrame.showImage(pipeline.inputFrame)
                }
            }

            pipeline.stop()
            restartRequested = false
        }

        canvasFrame.dispose()
        running = false
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
            InputProviderType.RealSense -> RealSenseInputProvider(config.input.deviceIndex.value, 1280, 720)
            //InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/nr88.mp4"))
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider())
        }
    }
}