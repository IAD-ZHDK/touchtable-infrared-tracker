package ch.zhdk.tracking.pipeline.detection

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.pipeline.PipelineStep
import org.bytedeco.opencv.opencv_core.Mat

abstract class RegionDetector(config : PipelineConfig = PipelineConfig()) : PipelineStep(config) {
    abstract fun detectRegions(frame: Mat, timestamp : Long): List<ActiveRegion>
}