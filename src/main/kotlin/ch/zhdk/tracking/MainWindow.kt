package ch.zhdk.tracking

import ch.bildspur.configuration.ConfigurationController
import ch.bildspur.math.Float2
import ch.bildspur.ui.fx.PropertiesControl
import ch.bildspur.util.format
import ch.zhdk.tracking.config.AppConfig
import ch.zhdk.tracking.javacv.*
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.state.TrackingEntityState
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toPoint
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.stage.Stage
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.AbstractScalar
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Rect
import kotlin.math.roundToInt

class MainWindow(val configController: ConfigurationController, val config: AppConfig) : Application() {
    private val windowName = "ZHdK - IR Tracker"
    private val propertiesControl = PropertiesControl()
    val canvas = Canvas(1280.0, 720.0)

    override fun start(primaryStage: Stage) {
        primaryStage.title = windowName

        val root = createUI(primaryStage)
        primaryStage.scene = Scene(root, canvas.width + 380, 720.0)

        primaryStage.setOnCloseRequest {
            TrackingApplication.running = false
        }

        primaryStage.show()
    }

    private fun createUI(primaryStage: Stage): Pane {
        // components
        val saveButton = Button("Save")
        saveButton.setOnAction {
            configController.saveAppConfig(config)
            println("config saved!")
            saveButton.style = "-fx-text-fill: #000000"
            primaryStage.title = windowName
        }
        saveButton.style = "-fx-text-fill: #000000"

        propertiesControl.initView(config)
        propertiesControl.propertyChanged += {
            primaryStage.title = "$windowName*"
            saveButton.style = "-fx-text-fill: #ff7675"
        }
        propertiesControl.maxWidth = Double.MAX_VALUE
        HBox.setHgrow(propertiesControl, Priority.ALWAYS)

        val spacerButton = Button("")
        spacerButton.isDisable = true

        val top = HBox(saveButton, spacerButton)

        val settings = mapOf(
            "General" to config,
            "Input" to config.input,
            "Pipeline" to config.pipeline,
            "Output" to config.output
        )

        settings.forEach { (name, cfg) ->
            val button = Button(name)
            button.setOnAction { propertiesControl.initView(cfg) }
            button.style = "-fx-font-size: 1em;"
            top.children.add(button)
        }

        // layout
        top.children.filterIsInstance<Button>().forEach {
            it.padding = Insets(5.0)
        }
        top.padding = Insets(10.0)
        top.spacing = 5.0

        return HBox(canvas, VBox(top, ScrollPane(propertiesControl)))
    }
}