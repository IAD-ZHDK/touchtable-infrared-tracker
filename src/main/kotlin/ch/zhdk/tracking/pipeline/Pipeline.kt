package ch.zhdk.tracking.pipeline

import ch.bildspur.event.Event
import ch.bildspur.model.math.Float2
import ch.bildspur.timer.ElapsedTimer
import ch.bildspur.util.Stopwatch
import ch.bildspur.util.format
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.javacv.image.GammaCorrection
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.math.roundToInt

abstract class Pipeline(
    val config: PipelineConfig,
    val inputProvider: InputProvider,
    val pipelineLock: Any = Any()
) {

    private lateinit var pipelineThread: Thread

    // watches
    private val frameWatch = Stopwatch()
    private val processWatch = Stopwatch()

    private val updateTimer = ElapsedTimer(200)

    @Volatile
    private var shutdownRequested = false

    private var startupLatch = CountDownLatch(1)

    private val gammaCorrection = GammaCorrection(config.gammaCorrection.value)

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
    val onObjectDetected = Event<TactileObject>()
    val onObjectRemoved = Event<TactileObject>()

    fun start() {
        if (isRunning)
            return

        startupLatch = CountDownLatch(1)
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
                        config.inputWidth.fire()
                        config.inputHeight.fire()
                        config.uniqueId.fire()
                    }

                    onFrameProcessed.invoke(this)
                } else {
                    Thread.sleep(1)
                }

                // mark that pipeline thead is up
                if (!isPipelineUp) {
                    isPipelineUp = true
                    startupLatch.countDown()
                }
            }
        }

        // wait till pipeline thread is up
        startupLatch.await()

        isRunning = true
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
        val inputMat = input.toMat()

        // set normalization values
        config.inputWidth.setSilent(input.imageWidth)
        config.inputHeight.setSilent(input.imageHeight)

        // exit if pipeline is not enabled
        if (!config.enabled.value) {
            // copy input frame
            synchronized(pipelineLock) {
                inputFrame = createBufferedImage(inputMat, inputFrame)

                // release input
                inputMat.release()
            }
            return true
        }

        // preprocessing

        if (config.enablePreProcessing.value) {
            // apply gamma correction
            gammaCorrection.correct(inputMat, config.gammaCorrection.value)
        }

        // get input mat
        val mat = inputMat.clone()

        // process
        val regions = detectRegions(mat, input.timestamp)
        mapRegionToObjects(tactileObjects, regions)
        recognizeObjectId(tactileObjects)

        // if no output should be shown (production)
        if (!config.displayOutput.value) {
            return true
        }

        // annotate
        synchronized(pipelineLock) {
            if (config.annotateOutput.value) {
                // annotate input
                annotateFrame(inputMat, regions)

                // annotate debug
                annotateFrame(mat, regions)
            }

            // lock frame reading
            processedFrame = createBufferedImage(mat, processedFrame)
            inputFrame = createBufferedImage(inputMat, inputFrame)

            // release
            mat.release()
            inputMat.release()
        }

        return true
    }

    abstract fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion>
    abstract fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>)
    abstract fun recognizeObjectId(objects: List<TactileObject>)

    private fun createBufferedImage(mat: Mat, image: BufferedImage): BufferedImage {
        if (mat.type() == CV_8UC1)
            mat.convertColor(opencv_imgproc.COLOR_GRAY2BGR)

        return mat.toFrame().createBufferedImageFast(image)
    }

    private fun annotateFrame(mat: Mat, regions: List<ActiveRegion>) {
        // convert to color if needed
        if (mat.type() == CV_8UC1)
            mat.convertColor(opencv_imgproc.COLOR_GRAY2BGR)

        // annotate pipeline output
        annotateActiveRegions(mat, regions)
        annotateTactileObjects(mat)

        // annotate screen calibration
        if (config.calibration.displayAnnotation.value) {
            annotateCalibration(mat)
        }
    }

    private fun annotateActiveRegions(mat: Mat, regions: List<ActiveRegion>) {
        // annotate active regions
        regions.forEach {
            // mark region
            mat.drawCircle(it.center.toPoint(), 20, AbstractScalar.RED, thickness = 1)

            // draw timestamp
            mat.drawText(
                "A: ${it.area} R: ${it.rotation.format(2)}",
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
    }

    private fun annotateTactileObjects(mat: Mat) {
        // annotate tactile objects
        tactileObjects.forEach {
            val color = if (it.deadTime == 0) AbstractScalar.GREEN else AbstractScalar.BLUE
            mat.drawCross(it.position.toPoint(), 22, color, thickness = 1)
            mat.drawText(
                "N:${it.uniqueId} #${it.identifier} [${it.lifeTime}]",
                it.position.toPoint().transform(20, -20),
                color,
                scale = 0.4
            )
        }
    }

    private fun annotateCalibration(mat: Mat) {
        // display edges and screen
        val screen = Float2(mat.width().toFloat(), mat.height().toFloat())

        val tl = screen * config.calibration.topLeft.value
        //val tr = screen * config.calibration.topRight.value
        val br = screen * config.calibration.bottomRight.value
        //val bl = screen * config.calibration.bottomLeft.value

        mat.drawMarker(tl.toPoint(), AbstractScalar.YELLOW, MARKER_CROSS)
        //mat.drawMarker(tr.toPoint(), AbstractScalar.YELLOW, MARKER_CROSS)
        mat.drawMarker(br.toPoint(), AbstractScalar.YELLOW, MARKER_CROSS)
        //mat.drawMarker(bl.toPoint(), AbstractScalar.YELLOW, MARKER_CROSS)

        val size = br - tl

        // draw screen
        val rect = Rect(tl.x.roundToInt(), tl.y.roundToInt(), size.x.roundToInt(), size.y.roundToInt())
        mat.drawRect(rect, AbstractScalar.GRAY)

        // todo: show polygon
        //mat.drawPolygon(listOf(tl.toPoint(), tr.toPoint(), br.toPoint(), bl.toPoint()), true, AbstractScalar.YELLOW)
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