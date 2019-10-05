package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.result.DetectionResult
import org.bytedeco.opencv.opencv_core.Mat

class SingleTrackingPipeline(config : PipelineConfig, inputProvider: InputProvider) : Pipeline(config, inputProvider) {
    override fun detectRegions(frame: Mat): DetectionResult {
        // find blobs


        return DetectionResult(emptyList())
    }

    override fun mapRegionToObjects(regions: List<ActiveRegion>, objects: List<TactileObject>) {

    }

    override fun analyzeObjectId(objects: List<TactileObject>) {

    }
}