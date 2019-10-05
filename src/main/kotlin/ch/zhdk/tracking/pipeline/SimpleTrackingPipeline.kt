package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.result.DetectionResult
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY
import org.bytedeco.opencv.opencv_core.Mat

class SimpleTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider) : Pipeline(config, inputProvider) {

    override fun detectRegions(frame: Mat): DetectionResult {
        // prepare frame for detection
        frame.convertColor(COLOR_BGR2GRAY)
        frame.threshold(config.threshold.value)

        // filter small elements
        if (config.morphologyFilterEnabled.value) {
            frame.erode(config.erodeSize.value)
            frame.dilate(config.dilateSize.value)
        }

        // read components
        val nativeComponents = frame.connectedComponentsWithStats()
        val components = nativeComponents.getConnectedComponents().filter { it.label != 0 }

        // convert to region
        val regions = components.map { ActiveRegion(it.centroid, it.area.toDouble()) }

        return DetectionResult(regions)
    }

    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {
        // reset all regions
        objects.forEach { it.isAlive = false }

        // create matrix
        matchNearest(objects, regions, config.maxDelta.value)

        // remove dead regions
        objects.removeAll { !it.isAlive }

        // update regions
        objects.forEach { it.lifeTime++ }

        // create new regions
        objects.addAll(regions.filter { !it.matched }.map { it.toTactileObject() })
    }

    override fun recognizeObjectId(objects: List<TactileObject>) {

    }

    private fun matchNearest(objects: MutableList<TactileObject>, regions: List<ActiveRegion>, maxDelta : Double) {
        // create matrix (point to region)
        val distances = Array(regions.size) { DoubleArray(objects.size) }

        // fill matrix O((m*n)^2)
        regions.forEachIndexed { i, region ->
            objects.forEachIndexed { j, obj ->
                distances[i][j] = obj.position.distance(region.position)
            }
        }

        // match best region to point
        regions.forEachIndexed { i, region ->
            val minDelta = distances[i].min() ?: Double.MAX_VALUE

            if (minDelta <= maxDelta) {
                val regionIndex = distances[i].indexOf(minDelta)
                objects[regionIndex].position = region.position
                objects[regionIndex].isAlive = true

                region.matched = true
            }
        }
    }
}