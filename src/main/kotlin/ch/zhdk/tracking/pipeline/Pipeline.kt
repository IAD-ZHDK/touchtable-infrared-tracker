package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.toFrame
import ch.zhdk.tracking.javacv.toMat
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.concurrent.thread

abstract class Pipeline(val inputProvider : InputProvider) {
    private val lock = java.lang.Object()

    private lateinit var pipelineThread : Thread
    @Volatile private var shutdownRequested = false

    var isRunning = false
        private set

    @Volatile var inputFrame : Frame = Frame(100, 100, 8, 3)
        @Synchronized get
        @Synchronized private set

    @Volatile var processedFrame : Frame = Frame(100, 100, 8, 3)
        @Synchronized get
        @Synchronized private set

    @Volatile var isZeroFrame = true
        private set

    fun start() {
        if(isRunning)
            return

        shutdownRequested = false

        // open input provider
        inputProvider.open()

        // start processing thread
        pipelineThread = thread(start = true) {
            while(!shutdownRequested) {
                // read frame

                val input = inputProvider.read()

                // check for zero size mats
                if(input.imageWidth == 0 || input.imageHeight == 0) {
                    isZeroFrame = true
                    continue
                }

                // reset zeroFrame flag
                isZeroFrame = false

                // set input frame
                inputFrame = input.clone()

                // process
                val mat = process(input.clone().toMat())

                // lock frame reading
                processedFrame = mat.toFrame()
            }
        }
    }

    abstract fun process(frame : Mat) : Mat

    fun stop() {
        if(!isRunning)
            return

        shutdownRequested = true
        pipelineThread.join()

        inputProvider.close()
    }
}