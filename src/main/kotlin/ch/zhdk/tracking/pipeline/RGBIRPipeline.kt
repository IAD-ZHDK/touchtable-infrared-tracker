package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.convertColor
import ch.zhdk.tracking.javacv.toPoint2d
import ch.zhdk.tracking.javacv.transform
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.opencv.global.opencv_core.*
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Point
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

            //rangeMat.convertColor(COLOR_HSV2BGR)
            //rangeMat.convertColor(COLOR_BGR2GRAY)

            // extract brightest point
            val minVal = DoublePointer()
            val maxVal = DoublePointer()
            val min = Point()
            val max = Point()
            minMaxLoc(mask, minVal, maxVal, min, max, null)

            // outline
            val result = Mat()
            bitwise_and(frame, frame, result, mask)



            // which one to display
            if(config.displayRangeOne.value && index == 1) {
                result.copyTo(frame)
            }

            if(!config.displayRangeOne.value && index == 0) {
                result.copyTo(frame)
            }

            result.release()

            ActiveRegion(max.toPoint2d().transform(5.0, 5.0),
                max,
                Size(10, 10),
                index.toDouble(),
                timestamp)
        }

        hsvMat.release()
        return regions
    }

    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {

    }

    override fun recognizeObjectId(objects: List<TactileObject>) {

    }
}