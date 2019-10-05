package ch.zhdk.tracking

import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.io.CameraInputProvider
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.io.InputProviderType
import ch.zhdk.tracking.io.VideoInputProvider
import ch.zhdk.tracking.pipeline.SingleTrackingPipeline
import org.bytedeco.javacv.CanvasFrame
import java.nio.file.Paths

class CVPreview(val config: AppConfig) {

    @Volatile
    var running = true

    fun start() {
        val canvasFrame = CanvasFrame("Preview")
        canvasFrame.setCanvasSize(1280, 720)

        val pipeline = SingleTrackingPipeline(createInputProvider())
        pipeline.start()

        while (running && canvasFrame.isVisible) {
            if (config.displayProcessed.value)
                canvasFrame.showImage(pipeline.processedFrame)
            else
                canvasFrame.showImage(pipeline.inputFrame)
        }

        canvasFrame.dispose()
    }

    fun createInputProvider(): InputProvider {
        return when (config.inputConfig.inputProvider.value) {
            InputProviderType.CameraInput -> CameraInputProvider(0, 1280, 720)
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/irMovieSample.mov"))
        }
    }
}