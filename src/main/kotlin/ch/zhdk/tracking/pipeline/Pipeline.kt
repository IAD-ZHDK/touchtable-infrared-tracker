package ch.zhdk.tracking.pipeline

import ch.bildspur.event.Event
import ch.bildspur.model.math.Float2
import ch.bildspur.timer.ElapsedTimer
import ch.bildspur.util.Stopwatch
import ch.bildspur.util.format
import ch.bildspur.util.formatSeconds
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.javacv.image.GammaCorrection
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.state.MarkerState
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.math.roundToInt

abstract class Pipeline(
    val config: PipelineConfig,
    val inputProvider: InputProvider,
    private val pipelineLock: Any = Any()
) {

    private lateinit var pipelineThread: Thread

    // watches
    private val frameWatch = Stopwatch()
    private val processWatch = Stopwatch()

    private val updateTimer = ElapsedTimer(200)

    @Volatile
    private var shutdownRequested = false

    // concurrency
    private var startupLatch = CountDownLatch(1)
    private val newFrameAvailableSemaphore = Semaphore(0)

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
    lateinit var annotationFrame: BufferedImage
        @Synchronized get
        @Synchronized private set

    @Volatile
    var isZeroFrame = true
        private set

    val markers = mutableListOf<Marker>()

    val onFrameProcessed = Event<Pipeline>()
    val onObjectDetected = Event<Marker>()
    val onObjectRemoved = Event<Marker>()

    fun waitForNewFrameAvailable() {
        newFrameAvailableSemaphore.acquire()
    }

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
        annotationFrame = BufferedImage(inputProvider.width, inputProvider.height, TYPE_3BYTE_BGR)

        // start processing thread
        pipelineThread = thread(start = true, name = "Pipeline Thread") {
            while (!shutdownRequested) {
                frameWatch.start()
                if (processFrame()) {
                    processWatch.stop()
                    frameWatch.stop()

                    // release new frame available
                    newFrameAvailableSemaphore.release()

                    // update info
                    if (updateTimer.elapsed()) {
                        config.frameTime.value = "${frameWatch.elapsed()} ms"
                        config.processingTime.value = "${processWatch.elapsed()} ms"
                        config.actualObjectCount.value = markers.count()
                        config.inputWidth.fire()
                        config.inputHeight.fire()
                        config.uniqueId.fire()
                    }

                    onFrameProcessed.invoke(this)

                    // mark that pipeline thead is up if first image has been read
                    if (!isPipelineUp) {
                        isPipelineUp = true
                        startupLatch.countDown()
                    }
                } else {
                    Thread.sleep(1)
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
        val inputMat = input.toMat().clone()

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

        // pre-processing
        if (config.enablePreProcessing.value) {
            // apply gamma correction
            gammaCorrection.correct(inputMat, config.gammaCorrection.value)
        }

        // get input mat
        val processedMat = inputMat.clone()
        val annotationMat = inputMat.zeros(CV_8UC3)

        // process
        val regions = detectRegions(processedMat, input.timestamp)
        mapRegionToObjects(markers, regions)
        recognizeObjectId(markers)

        // if no output should be shown (production)
        if (!config.displayOutput.value) {
            return true
        }

        // annotate
        if (config.annotateOutput.value) {
            annotateFrame(annotationMat, regions)
        }

        synchronized(pipelineLock) {
            // lock frame reading
            processedFrame = createBufferedImage(processedMat, processedFrame)
            inputFrame = createBufferedImage(inputMat, inputFrame)
            annotationFrame = createBufferedImage(annotationMat, annotationFrame)

            // release
            annotationMat.release()
            processedMat.release()
            inputMat.release()
        }

        return true
    }

    abstract fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion>
    abstract fun mapRegionToObjects(objects: MutableList<Marker>, regions: List<ActiveRegion>)
    abstract fun recognizeObjectId(objects: List<Marker>)

    private fun createBufferedImage(mat: Mat, image: BufferedImage): BufferedImage {
        if (mat.type() == CV_8UC1)
            mat.convertColor(COLOR_GRAY2BGR)

        return mat.toFrame().createBufferedImageFast(image)
    }

    private fun annotateFrame(mat: Mat, regions: List<ActiveRegion>) {
        // convert to color if needed
        if (mat.type() == CV_8UC1) {
            mat.convertColor(COLOR_GRAY2BGR)
        }

        // annotate pipeline output
        annotateActiveRegions(mat, regions)
        annotateMarkers(mat)

        // annotate screen calibration
        if (config.calibration.displayAnnotation.value) {
            annotateCalibration(mat)
        }
    }

    private fun annotateActiveRegions(mat: Mat, regions: List<ActiveRegion>) {
        // annotate active regions
        regions.forEach {
            // mark region
            mat.drawCross(it.center.toPoint(), 20, AbstractScalar.RED, thickness = 1)

            // show max distance
            mat.drawCircle(it.center.toPoint(), config.maxDelta.value.roundToInt(), AbstractScalar.RED, thickness = 1)

            // draw timestamp
            mat.drawText(
                "A: ${it.area} R: ${it.rotation.format(2)}",
                it.center.toPoint().transform(20, 20),
                AbstractScalar.RED,
                scale = 0.4
            )
        }
    }

    private fun annotateMarkers(mat: Mat) {
        // annotate tactile objects
        markers.forEach {
            val color = when (it.state) {
                MarkerState.Detected -> AbstractScalar.CYAN
                MarkerState.Alive -> AbstractScalar.GREEN
                MarkerState.Missing -> AbstractScalar.BLUE
                MarkerState.Dead -> AbstractScalar.YELLOW
            }

            // todo: check for NAN
            mat.drawCircle(it.position.toPoint(), 22, color, thickness = 1)
            mat.drawText(
                "N:${it.uniqueId} #${it.identifier} [${it.timeSinceLastStateChange.formatSeconds()}]",
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
    }

    fun stop() {
        if (!isRunning)
            return

        // do not block shutdown
        newFrameAvailableSemaphore.release()

        shutdownRequested = true
        pipelineThread.join(1000 * 10)

        isRunning = false
        isPipelineUp = false

        inputProvider.close()
    }
}