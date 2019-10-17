package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.detection.ConventionalRegionDetector
import ch.zhdk.tracking.pipeline.identification.BinaryObjectIdentifier
import ch.zhdk.tracking.pipeline.tracking.DistanceRegionTracker
import org.bytedeco.javacpp.FloatPointer
import org.bytedeco.opencv.global.opencv_core.CV_32F
import org.bytedeco.opencv.global.opencv_core.transform
import org.bytedeco.opencv.global.opencv_imgproc.getPerspectiveTransform
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point2d
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.librealsense.global.RealSense.points
import org.bytedeco.opencv.opencv_core.Point2f




class SimpleTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider, pipelineLock: Any = Any()) :
    Pipeline(config, inputProvider, pipelineLock) {

    private val regionDetector = ConventionalRegionDetector(config)
    private val regionTracker = DistanceRegionTracker(config)
    private val objectIdentifier = BinaryObjectIdentifier(config)

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        return regionDetector.detectRegions(frame, timestamp)
    }

    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {
        regionTracker.mapRegionToObjects(objects, regions)
        normalizeObjects(objects)
    }

    override fun recognizeObjectId(objects: List<TactileObject>) {
        if (config.identificationEnabled.value)
            objectIdentifier.recognizeObjectId(objects)
    }

    private fun normalizeObjects(objects: MutableList<TactileObject>) {
        // create perspective transform
        val inputMat =
            Mat(
                Size(2, 4), CV_32F,
                FloatPointer(
                    0f, 0f,
                    1f, 0f,
                    0f, 1f,
                    1f, 1f
                )
            )

        val outputMat = Mat(
            Size(2, 4), CV_32F,
            FloatPointer(
                config.calibration.topLeft.value.x, config.calibration.topLeft.value.y,
                config.calibration.topRight.value.x, config.calibration.topRight.value.y,
                config.calibration.bottomLeft.value.x, config.calibration.bottomLeft.value.y,
                config.calibration.bottomRight.value.x, config.calibration.bottomRight.value.y
            )
        )

        // create transform
        val transform = getPerspectiveTransform(inputMat, outputMat)

        // add normalized values
        objects.forEach {
            it.normalizedPosition = Point2d(
                it.position.x() / config.inputWidth.value,
                it.position.y() / config.inputHeight.value
            )
            it.normalizedIntensity = it.intensity / (config.inputWidth.value * config.inputHeight.value)

            // map position to defined calibration
            print("Pos: ${it.normalizedPosition.x()} | ${it.normalizedPosition.y()}\t")
            //val input = Mat(Size(2, 1), CV_32F, FloatPointer(it.normalizedPosition.x().toFloat(), it.normalizedPosition.y().toFloat()))
            //(input, input, transform)

            //val out = Point2f(input)

           // println("After: ${out.x()} | ${out.y()}")

            //it.mappedNormalizedPosition = transform.mul(it.normalizedPosition.)
        }
    }
}