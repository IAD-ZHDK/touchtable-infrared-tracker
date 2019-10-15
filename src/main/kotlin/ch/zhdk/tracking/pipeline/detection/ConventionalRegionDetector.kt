package ch.zhdk.tracking.pipeline.detection

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.javacv.contour.Contour
import ch.zhdk.tracking.model.ActiveRegion
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_OTSU
import org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_BINARY


class ConventionalRegionDetector(config: PipelineConfig = PipelineConfig()) : RegionDetector(config) {
    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        // prepare frame for detection
        if (frame.type() == opencv_core.CV_8UC3)
            frame.convertColor(opencv_imgproc.COLOR_BGR2GRAY)

        // running threshold
        if (config.useOTSUThreshold.value)
            frame.threshold(config.threshold.value, type = CV_THRESH_BINARY or CV_THRESH_OTSU)
        else
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
        if (config.detectOrientation.value) {
            components.forEachIndexed { i, component ->
                val roi = component.getROI(frame)
                val contours = MatVector()
                opencv_imgproc.findContours(
                    roi,
                    contours,
                    opencv_imgproc.CV_RETR_EXTERNAL,
                    opencv_imgproc.CV_CHAIN_APPROX_SIMPLE
                )

                for (i in 0 until contours.size()) {
                    val contour = Contour(contours.get(i))
                    //val minBox = contour.minAreaBox()

                    // calculate angle
                    //regions[i.toInt()].rotation = minBox.angle().toDouble()

                    //val indexer = approx.createIndexer<IntRawIndexer>()
                    //println("Rows: ${indexer.rows()} Cols: ${indexer.cols()}")

                    val approx = contour.approxPolyDP(config.approximationEpsilon.value)
                    if (i == 0L) {
                        regions[i.toInt()].polygon = approx
                    }
                }
            }
        }

        return regions
    }
}