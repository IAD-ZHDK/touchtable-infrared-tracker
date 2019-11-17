package ch.zhdk.tracking.pipeline.detection

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.javacv.analysis.ConnectedComponent
import ch.zhdk.tracking.javacv.contour.Contour
import ch.zhdk.tracking.model.ActiveRegion
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.global.opencv_imgproc.cvFitEllipse2
import org.bytedeco.javacpp.FloatPointer
import org.bytedeco.javacpp.indexer.IntRawIndexer
import org.bytedeco.opencv.global.opencv_core.CV_32FC2
import org.bytedeco.opencv.global.opencv_core.cvMat
import org.bytedeco.opencv.opencv_core.*


class ConventionalRegionDetector(config: PipelineConfig = PipelineConfig()) : RegionDetector(config) {
    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        // prepare frame for detection
        if (frame.type() == opencv_core.CV_8UC3)
            frame.convertColor(COLOR_BGR2GRAY)

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

        // create active regions (filter by size)
        return components
            .filter {  it.size.area() >= config.minAreaSize.value.toInt() }
            .map { ActiveRegion(it.centroid, it.position, it.size, it.area.toDouble(), timestamp) }
    }
}