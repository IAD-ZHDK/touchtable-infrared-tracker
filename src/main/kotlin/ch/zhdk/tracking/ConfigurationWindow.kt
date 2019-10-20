package ch.zhdk.tracking

import ch.bildspur.configuration.ConfigurationController
import ch.bildspur.ui.properties.PropertiesControl
import ch.zhdk.tracking.config.AppConfig
import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.effect.BlendMode
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import java.awt.image.BufferedImage

class ConfigurationWindow(private val configController: ConfigurationController, val config: AppConfig) : Application() {
    private val windowName = "ZHdK - IR Tracker"
    private val propertiesControl = PropertiesControl()
    private val canvas = Canvas(config.previewWidth.value.toDouble(), config.previewHeight.value.toDouble())
    private val gc = canvas.graphicsContext2D

    override fun start(primaryStage: Stage) {
        primaryStage.title = windowName

        val root = createUI(primaryStage)
        primaryStage.scene = Scene(
            root,
            config.previewWidth.value.toDouble() + propertiesControl.width,
            config.previewHeight.value.toDouble()
        )

        primaryStage.setOnShown {
            propertiesControl.resize(primaryStage.scene.width, primaryStage.scene.height)
        }

        primaryStage.setOnCloseRequest {
            TrackingApplication.running = false
        }

        val primScreenBounds = Screen.getPrimary().visualBounds
        primaryStage.x = primScreenBounds.width / 8.0 * 7.0

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

        val spacerButton = Button("")
        spacerButton.isDisable = true

        val top = HBox(saveButton, spacerButton)

        // create settings
        val settings = mapOf(
            "General" to config,
            "Input" to config.input,
            "Pipeline" to config.pipeline,
            "Calibration" to config.pipeline.calibration,
            "OSC" to config.osc
        )

        settings.forEach { (name, cfg) ->
            val button = Button(name)
            button.setOnAction { propertiesControl.initView(cfg) }
            button.style = "-fx-font-size: 1em;"
            top.children.add(button)
        }

        // setup canvas
        canvas.widthProperty().addListener { _, _, _ ->  }

        // layout
        top.children.filterIsInstance<Button>().forEach {
            it.padding = Insets(5.0)
        }
        top.padding = Insets(10.0)
        top.spacing = 5.0

        val scrollPane = ScrollPane(propertiesControl)
        scrollPane.isFitToWidth = true

        return BorderPane(canvas, top, scrollPane, null, null)
    }

    fun drawImage(image: Image, overlay: Image) {
        // clear screen
        gc.fill = Color.BLACK
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)

        // print image
        gc.drawImage(image, 0.0, 0.0)

        // print overlay
        gc.globalBlendMode = BlendMode.SCREEN
        gc.drawImage(overlay, 0.0, 0.0)
        gc.globalBlendMode = BlendMode.SRC_OVER
    }
}