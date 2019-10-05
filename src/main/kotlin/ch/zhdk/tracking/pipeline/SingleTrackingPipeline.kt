package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.result.DetectionResult
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.*;
import org.opencv.imgproc.Imgproc
import org.bytedeco.opencv.global.opencv_imgproc.*

class SingleTrackingPipeline(config : PipelineConfig, inputProvider: InputProvider) : Pipeline(config, inputProvider) {
    override fun detectRegions(frame: Mat): DetectionResult {
        // prepare frame for detection
        frame.convertColor(COLOR_BGR2GRAY)
        frame.threshold(config.threshold.value)

        // filter small elements
        if(config.morphologyFilterEnabled.value) {
            frame.erode(config.erodeSize.value)
            frame.dilate(config.dilateSize.value)
        }

        // read components
        val nativeComponents = frame.connectedComponentsWithStats()
        val components = nativeComponents.getConnectedComponents().filter { it.label != 0 }

        // convert to region
        val regions = components.map { ActiveRegion(it.centroid,  it.area.toDouble()) }

        return DetectionResult(regions)
    }

    override fun mapRegionToObjects(regions: List<ActiveRegion>, objects: List<TactileObject>) {

    }

    override fun analyzeObjectId(objects: List<TactileObject>) {

    }
}