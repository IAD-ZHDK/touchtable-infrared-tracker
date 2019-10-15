package ch.zhdk.tracking.pipeline

import ch.bildspur.event.Event
import ch.bildspur.timer.ElapsedTimer
import ch.bildspur.util.Stopwatch
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.drawContours
import org.bytedeco.opencv.opencv_core.AbstractScalar
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.Rect
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.awt.image.BufferedImage.TYPE_INT_RGB
import kotlin.concurrent.thread

abstract class Pipeline(val config: PipelineConfig, val inputProvider: InputProvider) {
    private val lock = java.lang.Object()

    private lateinit var pipelineThread: Thread

    // watches
    private val frameWatch = Stopwatch()
    private val processWatch = Stopwatch()

    private val updateTimer = ElapsedTimer(200)

    @Volatile
    private var shutdownRequested = false

    var isRunning = false
        private set

    @Volatile
    private var isPipelineUp = false

    @Volatile
    lateinit var inputFrame: BufferedImage
        @Synchronized get
        @Synchronized private set

    @Volatile
    lateinit var processedFrame: BufferedImage
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

        isRunning = true
        shutdownRequested = false

        // open input provider
        inputProvider.open()

        // create buffer for output
        inputFrame = BufferedImage(inputProvider.width, inputProvider.height, TYPE_3BYTE_BGR)
        processedFrame = BufferedImage(inputProvider.width, inputProvider.height, TYPE_3BYTE_BGR)

        // start processing thread
        pipelineThread = thread(start = true, name = "Pipeline Thread") {
            while (!shutdownRequested) {
                frameWatch.start()
                if (processFrame()) {
                    processWatch.stop()
                    frameWatch.stop()

                    // update info
                    if (updateTimer.elapsed()) {
                        config.frameTime.value = "${frameWatch.elapsed()} ms"
                        config.processingTime.value = "${processWatch.elapsed()} ms"
                        config.actualObjectCount.value = tactileObjects.count()
                        config.uniqueId.fire()
                    }

                    onFrameProcessed.invoke(this)
                }

                // mark that pipeline thead is up
                if(!isPipelineUp)
                    isPipelineUp = true
            }
        }

        // wait till pipeline thread is up
        while(!isPipelineUp) {
            Thread.sleep(10)
        }
    }

    private fun processFrame(): Boolean {
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

        // set pre process frame
        var preProcessFrame = input

        // exit if pipeline is not enabled
        if (!config.enabled.value) {
            // copy input frame
            println("clone preProcessFrame")
            //inputFrame = preProcessFrame.clone()
            return true
        }

        // set normalization values
        config.inputWidth.value = input.imageWidth
        config.inputHeight.value = input.imageHeight

        val mat = input.toMat()

        // process
        val regions = detectRegions(mat, input.timestamp)
        mapRegionToObjects(tactileObjects, regions)
        recognizeObjectId(tactileObjects)

        // if no output should be shown (production)
        if(!config.displayOutput.value) {
            return true
        }

        // annotate
        if (config.annotateOutput.value) {
            // annotate input
            val inputMat = preProcessFrame.toMat()
            annotateFrame(inputMat, regions)
            preProcessFrame = inputMat.toFrame()

            // annotate debug
            annotateFrame(mat, regions)
        }

        // lock frame reading
        processedFrame = mat.toFrame().createBufferedImageFast(processedFrame)
        inputFrame = preProcessFrame.createBufferedImageFast(inputFrame)
        return true
    }

    abstract fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion>
    abstract fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>)
    abstract fun recognizeObjectId(objects: List<TactileObject>)

    private fun annotateFrame(mat: Mat, regions: List<ActiveRegion>) {
        // convert to color if needed
        if (mat.type() == CV_8UC1)
            mat.convertColor(opencv_imgproc.COLOR_GRAY2BGR)

        // annotate active regions
        regions.forEach {
            // mark region
            mat.drawCircle(it.center.toPoint(), 20, AbstractScalar.RED, thickness = 1)

            // draw timestamp
            mat.drawText(
                "A: ${it.area} R: ${it.rotation}",
                it.center.toPoint().transform(20, 20),
                AbstractScalar.RED,
                scale = 0.4
            )

            // display shape
            if (it.polygon.rows() > 0) {
                val rect = Rect(it.position.x(), it.position.y(), it.size.width(), it.size.height())
                drawContours(mat.checkedROI(rect), MatVector(it.polygon), 0, AbstractScalar.CYAN)
            }
        }

        // annotate tactile objects
        tactileObjects.forEach {
            mat.drawCross(it.position.toPoint(), 22, AbstractScalar.GREEN, thickness = 1)
            mat.drawText(
                "N:${it.uniqueId} #${it.identifier} [${it.lifeTime}]",
                it.position.toPoint().transform(20, -20),
                AbstractScalar.GREEN,
                scale = 0.4
            )
        }
    }

    fun stop() {
        if (!isRunning)
            return

        shutdownRequested = true
        pipelineThread.join(5000)

        isRunning = false
        isPipelineUp = false

        inputProvider.close()
    }
}