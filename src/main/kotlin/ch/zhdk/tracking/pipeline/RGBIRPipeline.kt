package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Point2d
import org.bytedeco.opencv.opencv_core.Size
import kotlin.math.roundToInt


class RGBIRPipeline(config: PipelineConfig, inputProvider: InputProvider, pipelineLock: Any = Any()) :
    Pipeline(config, inputProvider, pipelineLock) {

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        // detect red channels
        val hsvMat = Mat()
        frame.convertColor(hsvMat, COLOR_BGR2HSV)

        // extract color range and detect max light
        val regions = listOf(config.hueRangeZero.value, config.hueRangeOne.value).mapIndexed { index, range ->
            val mask = Mat()
            val lowerColor = Mat(1, 1, CV_32SC4, IntPointer((range.lowValue * 255.0).roundToInt(), 120, 70, 0))
            val higherColor = Mat(1, 1, CV_32SC4, IntPointer((range.highValue * 255.0).roundToInt(), 255, 255, 0))

            // extract color
            inRange(hsvMat, lowerColor, higherColor, mask)

            // outline
            val result = Mat()
            bitwise_and(frame, frame, result, mask)

            // which one to display
            if(config.displayRangeOne.value && index == 1) {
                result.clone().copyTo(frame)
            }

            if(!config.displayRangeOne.value && index == 0) {
                result.clone().copyTo(frame)
            }

            // extract brightest point
            result.convertColor(COLOR_BGR2GRAY)

            // filter
            if(config.morphologyFilterEnabled.value) {
                frame.erode(config.erodeSize.value)
                frame.dilate(config.dilateSize.value)
            }

            /*
            // minmaxloc
            val minVal = DoublePointer()
            val maxVal = DoublePointer()
            val min = Point()
            val max = Point()
            minMaxLoc(result, minVal, maxVal, min, max, null)
             */

            // detection through average
            val nonZeroPixels = Mat()
            findNonZero(result, nonZeroPixels)
            val mm = moments(nonZeroPixels, false )
            val moment10 = mm.m10()
            val moment01 = mm.m01()
            val moment00 = mm.m00()
            val point = Point2d(moment10 / moment00, moment01 / moment00)

            // fix nan bug
            if(point.x().isNaN() || point.y().isNaN()) {
                point.x(0.0)
                point.y(0.0)
            }

            result.release()

            // create active region
            ActiveRegion(point,
                point.toPoint(),
                Size(10, 10),
                index.toDouble(),
                timestamp)
        }

        hsvMat.release()
        return regions
    }

    override fun mapRegionToObjects(objects: MutableList<Marker>, regions: List<ActiveRegion>) {

    }

    override fun recognizeObjectId(objects: List<Marker>) {

    }
}