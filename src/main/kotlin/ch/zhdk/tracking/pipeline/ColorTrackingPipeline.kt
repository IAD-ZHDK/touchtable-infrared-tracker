package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.pipeline.detection.ColorRegionDetector
import org.bytedeco.opencv.opencv_core.Mat

class ColorTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider, pipelineLock: Any = Any()) :
    SimpleTrackingPipeline(config, inputProvider, pipelineLock) {

    protected val colorRegionDetector = ColorRegionDetector(config)

    init {
        steps.remove(regionDetector)
        steps.add(0, colorRegionDetector)
    }

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        return colorRegionDetector.detectRegions(frame, timestamp)
    }
}