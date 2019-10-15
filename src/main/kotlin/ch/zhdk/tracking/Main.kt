package ch.zhdk.tracking

import ch.bildspur.configuration.ConfigurationController
import ch.zhdk.tracking.config.AppConfig
import javafx.application.Platform
import javafx.stage.Stage
import org.bytedeco.javacpp.Loader
import org.bytedeco.javacv.RealSense2FrameGrabber
import org.bytedeco.opencv.global.opencv_core
import kotlin.concurrent.thread


class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main().startApplication(args)
        }
    }

    fun startApplication(args: Array<String>) {
        val configuration = ConfigurationController("IR Tracking", "zhdk", "ir-tracking")
        var appConfig = configuration.loadAppConfig()

        // initialize opencv (needed for realsense camera)
        Loader.load(opencv_core::class.java)

        // use a fresh config while debugging
        if (args.contains("-dev"))
            appConfig = AppConfig()

        // start configuration app
        Platform.startup {
            val window = ConfigWindow(configuration, appConfig)
            val stage = Stage()
            window.start(stage)
        }

        //createRealSenseTest()

        // start main app
        CVPreview.start(appConfig)
    }

    fun createRealSenseTest() {
        // create realsense
        thread {
            println("starting realsense:")
            val rs2 = RealSense2FrameGrabber()
            rs2.enableColorStream(640, 480, 30)
            rs2.start()

            val frame = rs2.grab()

            println("Frame: ${frame.imageWidth} / ${frame.imageHeight}")
        }
    }
}