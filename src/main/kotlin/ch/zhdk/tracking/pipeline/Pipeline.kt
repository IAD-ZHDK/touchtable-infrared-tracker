package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.result.DetectionResult
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.global.opencv_core.CV_32SC4
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.AbstractScalar
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Scalar
import kotlin.concurrent.thread

abstract class Pipeline(val config: PipelineConfig, val inputProvider: InputProvider) {
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
                mapRegionToObjects(tactileObjects, detections.regions)
                recognizeObjectId(tactileObjects)

                // annotate
                if(config.annotateOutput.value) {
                    // annotate input
                    val inputMat = inputFrame.toMat()
                    annotateFrame(inputMat, detections.regions)
                    inputFrame = inputMat.toFrame()

                    // annotate debug
                    annotateFrame(mat, detections.regions)
                }

                // lock frame reading
                processedFrame = mat.toFrame()
            }
        }
    }

    abstract fun detectRegions(frame: Mat): DetectionResult
    abstract fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>)
    abstract fun recognizeObjectId(objects: List<TactileObject>)

    protected fun ActiveRegion.toTactileObject(): TactileObject {
        val tactileObject = TactileObject()
        this.toTactileObject(tactileObject)
        return tactileObject
    }

    protected fun ActiveRegion.toTactileObject(tactileObject : TactileObject)
    {
        tactileObject.position = this.position
        tactileObject.intensities.add(this.intensity)
    }

    private fun annotateFrame(mat: Mat, regions: List<ActiveRegion>) {
        // convert to color if needed
        if(mat.type() == CV_8UC1)
            mat.convertColor(opencv_imgproc.COLOR_GRAY2BGR)

        // annotate active regions
        regions.forEach {
            mat.drawCircle(it.position.toPoint(), 20, AbstractScalar.RED, thickness = 2)
        }

        // annotate tactile objects
        tactileObjects.forEach {
            mat.drawCross(it.position.toPoint(), 10, AbstractScalar.GREEN, thickness = 2)
            mat.drawText("${it.id} [${it.lifeTime}]",
                it.position.toPoint().transform(20, -20),
                AbstractScalar.GREEN,
                scale = 0.4)

            mat.drawText(it.intensities.joinToString(separator = ", ") { i -> i.toString() },
                it.position.toPoint().transform(20, 20),
                AbstractScalar.CYAN,
                scale = 0.3)
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