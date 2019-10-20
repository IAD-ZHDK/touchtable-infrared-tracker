package ch.zhdk.tracking

import ch.bildspur.configuration.ConfigurationController
import ch.zhdk.tracking.config.AppConfig
import javafx.application.Platform
import javafx.stage.Stage
import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_core


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
        val window = ConfigurationWindow(configuration, appConfig)
        TrackingApplication.configurationWindow = window

        Platform.startup {
            val stage = Stage()
            window.start(stage)
        }

        // start main app
        TrackingApplication.start(appConfig)
    }
}