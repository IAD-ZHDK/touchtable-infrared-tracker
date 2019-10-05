package ch.zhdk.tracking

import ch.zhdk.tracking.io.InputProvider
import ch.bildspur.thread.ProcessingInvoker
import ch.bildspur.thread.ProcessingTask
import ch.bildspur.timer.Timer
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.io.CameraInputProvider
import ch.zhdk.tracking.io.InputProviderType
import ch.zhdk.tracking.io.VideoInputProvider
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.pipeline.SingleTrackingPipeline
import org.bytedeco.javacv.OpenCVFrameGrabber
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage
import java.nio.file.Paths
import kotlin.math.roundToInt


class Application(val config: AppConfig) : PApplet() {
    companion object {
        @JvmStatic
        val NAME = "ZHdK IR Tracker"

        @JvmStatic
        val VERSION = "0.1.0"

        @JvmStatic
        val URI_NAME = "ir-tracker"

        @JvmStatic
        fun map(value: Double, start1: Double, stop1: Double, start2: Double, stop2: Double): Double {
            return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
        }

        @JvmStatic
        fun map(value: Int, start1: Int, stop1: Int, start2: Int, stop2: Int): Int {
            return map(
                value.toDouble(),
                start1.toDouble(),
                stop1.toDouble(),
                start2.toDouble(),
                stop2.toDouble()
            ).roundToInt()
        }
    }

    val inputWidth = 1280
    val inputHeight = 720

    var setupFinished = false
    var currentFrame = PImage(inputWidth, inputHeight, PConstants.ARGB)

    val invoker = ProcessingInvoker()

    val inputProvider : InputProvider = createInputProvider()

    val pipeline = SingleTrackingPipeline(config.pipeline, inputProvider)

    private val timer = Timer()

    override fun settings() {
        // setup main window
        if (config.visual.fullScreen.value)
            fullScreen(PConstants.FX2D, config.visual.screenIndex.value)
        else
            size(config.visual.width.value, config.visual.height.value, PConstants.FX2D)

        // setup screen density
        pixelDensity(config.visual.pixelDensity.value)
    }

    override fun setup() {
        surface.setTitle("$NAME - $VERSION")
        surface.setLocation(100, (displayHeight - height) / 2)

        frameRate(config.visual.frameRate.value.toFloat())
    }

    override fun draw() {
        if (!setupFinished)
            setupControllers()

        background(12f)

        // show debug output
        val lastFrame = pipeline.processedFrame

        // convert if size matches
        if(!pipeline.isZeroFrame && lastFrame.imageWidth == currentFrame.width) {
            pipeline.processedFrame.toPImage(currentFrame)
        }

        // draw image onto the screen
        image(currentFrame, 0f, 0f)

        timer.update()
        invoker.invokeTasks()

        text("FPS: $frameRate", 20f, 20f)
    }

    override fun stop() {
        pipeline.stop()
    }

    private fun setupControllers() {
        OpenCVFrameGrabber.list.forEach { kotlin.io.println(it) }

        timer.setup()
        pipeline.start()

        setupFinished = true
    }

    fun run() {
        runSketch()
    }

    override fun mouseMoved() {
        super.mouseMoved()
        cursor()
    }

    override fun keyPressed() {
    }

    fun createInputProvider() : InputProvider {
        return when(config.inputConfig.inputProvider.value) {
            InputProviderType.CameraInput ->  CameraInputProvider(0, inputWidth, inputHeight)
            InputProviderType.VideoInput -> VideoInputProvider(Paths.get("data/irMovieSample.mov"))
        }
    }

    fun takeScreenshot(file : String) {
        this.invoker.addTaskAndWait(ProcessingTask({
            saveFrame(file)
        }))
    }

    fun invokeOnProcessing(block: () -> Unit) {
        this.invoker.addTask(ProcessingTask(block))
    }

    fun ifDebug(block: () -> Unit) {
        if (!this.config.debuggingMode.value) return
        block()
    }
}