package ch.zhdk.tracking

import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.io.CameraInputProvider
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.io.InputProviderType
import ch.zhdk.tracking.io.VideoInputProvider
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineType
import ch.zhdk.tracking.pipeline.SimpleTrackingPipeline
import org.bytedeco.javacv.CanvasFrame
import java.nio.file.Paths
import javax.swing.WindowConstants

class CVPreview(val config: AppConfig) {

    @Volatile
    var running = true

    fun start() {
        val canvasFrame = CanvasFrame("Preview")
        canvasFrame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        canvasFrame.setCanvasSize(1280, 720)

        val pipeline = createPipeline()
        pipeline.start()

        while (running && canvasFrame.isVisible) {
            if (config.displayProcessed.value)
                canvasFrame.showImage(pipeline.processedFrame)
            else
                canvasFrame.showImage(pipeline.inputFrame)
        }

        pipeline.stop()
        canvasFrame.dispose()
    }

    private fun createInputProvider(): InputProvider {
        return when (config.inputConfig.inputProvider.value) {
            InputProviderType.CameraInput -> CameraInputProvider(0, 1280, 720)
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/irMovieSample.mov"), 30.0)
        }
    }

    private fun createPipeline(): Pipeline {
        return when (config.pipeline.pipelineType.value) {
            PipelineType.Simple -> SimpleTrackingPipeline(config.pipeline, createInputProvider())
        }
    }
}