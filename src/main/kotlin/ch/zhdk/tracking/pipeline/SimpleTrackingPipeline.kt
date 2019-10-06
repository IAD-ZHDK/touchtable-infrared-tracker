package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.detection.ConventionalRegionDetector
import ch.zhdk.tracking.pipeline.identification.BinaryObjectIdentifier
import ch.zhdk.tracking.pipeline.tracking.DistanceRegionTracker
import org.bytedeco.opencv.opencv_core.Mat


class SimpleTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider) : Pipeline(config, inputProvider) {
    private val regionDetector = ConventionalRegionDetector(config)
    private val regionTracker = DistanceRegionTracker(config)
    private val objectIdentifier = BinaryObjectIdentifier(config)

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        return regionDetector.detectRegions(frame, timestamp)
    }

    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {
        regionTracker.mapRegionToObjects(objects, regions)
    }

    override fun recognizeObjectId(objects: List<TactileObject>) {
        objectIdentifier.recognizeObjectId(objects)
    }

}