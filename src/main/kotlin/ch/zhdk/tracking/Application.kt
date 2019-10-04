package ch.zhdk.tracking

import ch.zhdk.tracking.thread.ProcessingInvoker
import ch.zhdk.tracking.thread.ProcessingTask
import ch.zhdk.tracking.timer.Timer
import ch.zhdk.tracking.timer.TimerTask
import ch.zhdk.tracking.model.config.AppConfig
import processing.core.PApplet
import processing.core.PConstants
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


    var setupFinished = false

    val invoker = ProcessingInvoker()

    private val timer = Timer()

    var lastCursorMoveTime = 0
    var cursorHideTime = 1000 * 5L

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
        frameRate(config.visual.frameRate.value.toFloat())
    }

    override fun draw() {
        if (!setupFinished)
            setupControllers()

        background(12f)
        timer.update()
        invoker.invokeTasks()
    }

    override fun stop() {

    }

    private fun setupControllers() {
        timer.setup()

        // timer for cursor hiding
        timer.addTask(TimerTask(cursorHideTime, {
            val current = millis()
            if (current - lastCursorMoveTime > cursorHideTime)
                noCursor()
        }, "CursorHide"))

        setupFinished = true
    }

    fun run() {
        runSketch()
    }

    override fun mouseMoved() {
        super.mouseMoved()
        cursor()
        lastCursorMoveTime = millis()
    }

    override fun keyPressed() {
        if (key == 'r') {
            //sceneManager.scanScene.syncTimeoutTask.lastMillis = 0

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