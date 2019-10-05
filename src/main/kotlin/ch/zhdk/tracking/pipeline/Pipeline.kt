package ch.zhdk.tracking.pipeline

import ch.bildspur.event.Event
import ch.bildspur.util.Stopwatch
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.AbstractScalar
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.concurrent.thread

abstract class Pipeline(val config: PipelineConfig, val inputProvider: InputProvider) {
    private val lock = java.lang.Object()

    private lateinit var pipelineThread: Thread

    // watches
    private val frameWatch = Stopwatch()
    private val processWatch = Stopwatch()

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

    val onFrameProcessed = Event<Pipeline>()

    fun start() {
        if (isRunning)
            return

        shutdownRequested = false

        // open input provider
        inputProvider.open()

        // start processing thread
        pipelineThread = thread(start = true) {
            while (!shutdownRequested) {
                frameWatch.start()
                if(processFrame()) {
                    processWatch.stop()
                    frameWatch.stop()

                    // update info
                    config.frameTime.value = "${frameWatch.elapsed()} ms"
                    config.processingTime.value = "${processWatch.elapsed()} ms"

                    onFrameProcessed.invoke(this)
                }
            }
        }
    }

    private fun processFrame() : Boolean {
        // read frame
        val input = inputProvider.read()

        // check for zero size mats
        if (input.imageWidth == 0 || input.imageHeight == 0) {
            isZeroFrame = true
            return false
        }

        processWatch.start()

        // reset zeroFrame flag
        isZeroFrame = false

        // set input frame
        inputFrame = input.clone()


        val mat = input.toMat()

        // process
        val regions = detectRegions(mat, input.timestamp)
        mapRegionToObjects(tactileObjects, regions)
        recognizeObjectId(tactileObjects)

        // annotate
        if(config.annotateOutput.value) {
            // annotate input
            val inputMat = inputFrame.toMat()
            annotateFrame(inputMat, regions)
            inputFrame = inputMat.toFrame()

            // annotate debug
            annotateFrame(mat, regions)
        }

        // lock frame reading
        processedFrame = mat.toFrame()
        return true
    }

    abstract fun detectRegions(frame: Mat, timestamp : Long): List<ActiveRegion>
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

            mat.drawText("${it.timestamp}",
                it.position.toPoint().transform(20, 20),
                AbstractScalar.RED,
                scale = 0.4)
        }

        // annotate tactile objects
        tactileObjects.forEach {
            mat.drawCross(it.position.toPoint(), 10, AbstractScalar.GREEN, thickness = 2)
            mat.drawText("${it.id} [${it.lifeTime}]",
                it.position.toPoint().transform(20, -20),
                AbstractScalar.GREEN,
                scale = 0.4)

            /*
            mat.drawText(it.intensities.joinToString(separator = ", ") { i -> i.toString() },
                it.position.toPoint().transform(20, 20),
                AbstractScalar.CYAN,
                scale = 0.3)

             */
        }
    }

    fun stop() {
        if (!isRunning)
            return

        shutdownRequested = true
        pipelineThread.join(5000)

        inputProvider.close()
    }
}