package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.javacv.contour.Contour
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*


class SimpleTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider) : Pipeline(config, inputProvider) {

    // detection

    override fun detectRegions(frame: Mat, timestamp : Long): List<ActiveRegion> {
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

        // create active regions
        val regions = components.map { ActiveRegion(it.centroid, it.position, it.size, it.area.toDouble(), timestamp) }

        // find orientation
        // todo: implement find contours for components
        if(config.detectOrientation.value) {
            components.forEachIndexed { i, component ->
                val roi = component.getROI(frame)
                val contours = MatVector()
                findContours(roi, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE)

                for(i in 0 until contours.size()) {
                    val contour = Contour(contours.get(i))
                    val approx = contour.approxPolyDP(config.approximationEpsilon.value)

                    //val indexer = approx.createIndexer<IntRawIndexer>()
                    //println("Rows: ${indexer.rows()} Cols: ${indexer.cols()}")

                    if(i == 0L) {
                        regions[i.toInt()].polygon = approx
                    }
                }
            }
        }

        return regions
    }

    // tracking

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

    private fun matchNearest(objects: MutableList<TactileObject>, regions: List<ActiveRegion>, maxDelta : Double) {
        // create matrix (point to region)
        val distances = Array(regions.size) { DoubleArray(objects.size) }

        // fill matrix O((m*n)^2)
        regions.forEachIndexed { i, region ->
            objects.forEachIndexed { j, obj ->
                distances[i][j] = obj.position.distance(region.center)
            }
        }

        // match best region to point
        regions.forEachIndexed { i, region ->
            val minDelta = distances[i].min() ?: Double.MAX_VALUE

            if (minDelta <= maxDelta) {
                val regionIndex = distances[i].indexOf(minDelta)
                region.toTactileObject(objects[regionIndex])
                objects[regionIndex].isAlive = true

                region.matched = true
            }
        }
    }

    // id recognition

    override fun recognizeObjectId(objects: List<TactileObject>) {

    }
}