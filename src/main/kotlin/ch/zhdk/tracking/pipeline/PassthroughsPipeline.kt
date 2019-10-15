package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.opencv.opencv_core.Mat

class PassthroughPipeline(config: PipelineConfig, inputProvider: InputProvider, pipelineLock: Any = Any()) :
    Pipeline(config, inputProvider, pipelineLock) {

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        return emptyList()
    }

    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {

    }

    override fun recognizeObjectId(objects: List<TactileObject>) {

    }

}