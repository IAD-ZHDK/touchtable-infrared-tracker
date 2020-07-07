package ch.zhdk.tracking.pipeline.detection

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import kotlinx.coroutines.selects.select
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*


class ConventionalRegionDetector(config: PipelineConfig = PipelineConfig()) : RegionDetector(config) {
    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        // prepare frame for detection
        if (frame.type() == opencv_core.CV_8UC3)
            frame.convertColor(COLOR_BGR2GRAY)

        // running binarization method
        when(config.binarizationMethod.value) {
            BinarizationMethod.Normal -> frame.threshold(config.threshold.value)
            BinarizationMethod.Adaptive -> frame.adaptiveThreshold(config.threshold.value, constant = config.adaptiveness.value)
            BinarizationMethod.OTSU -> frame.threshold(config.threshold.value, type = CV_THRESH_BINARY or CV_THRESH_OTSU)
            BinarizationMethod.Radial -> println("not implemented yet")
        }

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