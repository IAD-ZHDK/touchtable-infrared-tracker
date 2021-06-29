package ch.zhdk.tracking.pipeline.detection

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.connectedComponentsWithStats
import ch.zhdk.tracking.javacv.convertColor
import ch.zhdk.tracking.javacv.dilate
import ch.zhdk.tracking.javacv.erode
import ch.zhdk.tracking.model.ActiveRegion
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Scalar


class ColorRegionDetector(config: PipelineConfig = PipelineConfig()) : RegionDetector(config) {
    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        // check if is color frame
        if (frame.type() != opencv_core.CV_8UC3)
            error("no color frame!")

        // running binarization method
        val hsvFrame = Mat()
        frame.convertColor(hsvFrame, COLOR_BGR2HSV)

        opencv_core.inRange(
            hsvFrame,
            Mat(
                Scalar(
                    config.hueRange.value.low.map(360.0),
                    config.saturationRange.value.low.map(100.0),
                    config.valueRange.value.low.map(100.0),
                    0.0
                )
            ),
            Mat(
                Scalar(
                    config.hueRange.value.high.map(360.0),
                    config.saturationRange.value.high.map(100.0),
                    config.valueRange.value.high.map(100.0),
                    0.0
                )
            ),
            frame
        )

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
            .filter { it.size.area() >= config.minAreaSize.value.toInt() }
            .map { ActiveRegion(it.centroid, it.position, it.size, it.area.toDouble(), timestamp) }
    }

    fun Double.map(max: Double): Double {
        return this / max * 255.0
    }
}