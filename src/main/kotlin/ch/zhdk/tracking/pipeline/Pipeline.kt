package ch.zhdk.tracking.pipeline

import ch.bildspur.event.Event
import ch.bildspur.timer.ElapsedTimer
import ch.bildspur.util.Stopwatch
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.convertColor
import ch.zhdk.tracking.javacv.createBufferedImageFast
import ch.zhdk.tracking.javacv.image.GammaCorrection
import ch.zhdk.tracking.javacv.toFrame
import ch.zhdk.tracking.javacv.toMat
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_GRAY2BGR
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

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
    var regions = emptyList<ActiveRegion>()
    val devices = mutableListOf<TactileDevice>()

    val onFrameProcessed = Event<Pipeline>()
    val onDeviceDetected = Event<TactileDevice>()
    val onDeviceRemoved = Event<TactileDevice>()

    protected val steps = mutableListOf<PipelineStep>()

    var pipelineStartTimeStamp = 0L

    fun timeSincePipelineStart() : Long {
        return System.currentTimeMillis() - pipelineStartTimeStamp
    }

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

        // starting pipeline steps
        steps.forEach { it.pipelineStartup() }

        pipelineStartTimeStamp = System.currentTimeMillis()

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
                        config.actualObjectCount.value = devices.count()
                        config.inputWidth.fire()
                        config.inputHeight.fire()
                        config.uniqueMarkerId.fire()
                        config.uniqueTactileObjectId.fire()
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

        // process
        regions = detectRegions(processedMat, input.timestamp)
        mapRegionsToMarkers(markers, regions)
        clusterMarkersToDevices(markers, devices)
        recognizeObjectId(devices)

        // if no output should be shown (production)
        if (!config.displayOutput.value) {
            return true
        }

        synchronized(pipelineLock) {
            // lock frame reading
            // todo: only do this one time (for the one needed)
            processedFrame = createBufferedImage(processedMat, processedFrame)
            inputFrame = createBufferedImage(inputMat, inputFrame)

            // release
            processedMat.release()
            inputMat.release()
        }

        return true
    }

    abstract fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion>
    abstract fun mapRegionsToMarkers(markers: MutableList<Marker>, regions: List<ActiveRegion>)
    abstract fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: MutableList<TactileDevice>)
    abstract fun recognizeObjectId(devices: List<TactileDevice>)

    private fun createBufferedImage(mat: Mat, image: BufferedImage): BufferedImage {
        if (mat.type() == CV_8UC1)
            mat.convertColor(COLOR_GRAY2BGR)

        return mat.toFrame().createBufferedImageFast(image)
    }

    fun stop() {
        if (!isRunning)
            return

        // do not block shutdown
        newFrameAvailableSemaphore.release()

        shutdownRequested = true

        // stopping pipeline threads
        steps.forEach { it.pipelineStop() }

        pipelineThread.join(1000 * 10)

        isRunning = false
        isPipelineUp = false

        inputProvider.close()
    }
}