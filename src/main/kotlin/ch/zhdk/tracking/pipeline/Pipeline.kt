package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.result.DetectionResult
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.AbstractScalar
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.concurrent.thread

abstract class Pipeline(val config : PipelineConfig, val inputProvider: InputProvider) {
    private val lock = java.lang.Object()

    private lateinit var pipelineThread: Thread
    @Volatile
    private var shutdownRequested = false

    var isRunning = false
        private set

    @Volatile
    var inputFrame: Frame = Frame(100, 100, 8, 3)
        @Synchronized get
        @Synchronized private set

    @Volatile
    var processedFrame: Frame = Frame(100, 100, 8, 3)
        @Synchronized get
        @Synchronized private set

    @Volatile
    var isZeroFrame = true
        private set

    val tactileObjects = mutableListOf<TactileObject>()

    fun start() {
        if (isRunning)
            return

        shutdownRequested = false

        // open input provider
        inputProvider.open()

        // start processing thread
        pipelineThread = thread(start = true) {
            while (!shutdownRequested) {
                // read frame

                val input = inputProvider.read()

                // check for zero size mats
                if (input.imageWidth == 0 || input.imageHeight == 0) {
                    isZeroFrame = true
                    continue
                }

                // reset zeroFrame flag
                isZeroFrame = false

                // set input frame
                inputFrame = input.clone()


                val mat = input.toMat()

                // process
                val detections = detectRegions(mat)
                mapRegionToObjects(detections.regions, tactileObjects)
                analyzeObjectId(tactileObjects)

                // annotate
                annotateFrame(mat, detections.regions)

                // lock frame reading
                processedFrame = mat.toFrame()
            }
        }
    }

    abstract fun detectRegions(frame: Mat): DetectionResult
    abstract fun mapRegionToObjects(regions: List<ActiveRegion>, objects: List<TactileObject>)
    abstract fun analyzeObjectId(objects: List<TactileObject>)

    private fun annotateFrame(mat : Mat, regions : List<ActiveRegion>) {
        mat.convertColor(opencv_imgproc.COLOR_GRAY2BGR)

        // annotate active regions
        regions.forEach {
            mat.drawCircle(it.position.toPoint(), 20,  AbstractScalar.RED, thickness = 3)
        }
    }

    fun stop() {
        if (!isRunning)
            return

        shutdownRequested = true
        pipelineThread.join()

        inputProvider.close()
    }
}