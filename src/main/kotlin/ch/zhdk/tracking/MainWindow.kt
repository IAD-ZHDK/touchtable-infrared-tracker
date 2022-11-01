package ch.zhdk.tracking

import ch.bildspur.configuration.ConfigurationController
import ch.bildspur.ui.fx.PropertiesControl
import ch.zhdk.tracking.config.AppConfig
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.GridPane.setHgrow
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.lang.Double.max

class MainWindow(val configController: ConfigurationController, val config: AppConfig) : Application() {
    private val windowName = "ZHdK IAD - IR Tracker"
    private val propertiesControl = PropertiesControl()

    val canvas = Canvas(1280.0, 720.0)
    private lateinit var stage : Stage

    private val sideBarWidth = 380.0

    override fun start(primaryStage: Stage) {
        primaryStage.title = windowName

        config.previewSize.onChanged += {
            adjustWindowSize()
        }

        val root = createUI(primaryStage)
        primaryStage.scene = Scene(root)
        stage = primaryStage

        stage.isResizable = true
        stage.scene.widthProperty().addListener { _, _, _ -> adjustSize(stage.scene) }
        stage.scene.heightProperty().addListener { _, _, _ -> adjustSize(stage.scene) }

        primaryStage.setOnShown {
            this.canvas.requestFocus()
        }

        primaryStage.setOnCloseRequest {
            TrackingApplication.running = false
        }

        config.previewSize.fire()

        primaryStage.show()
    }

    private fun adjustSize(scene: Scene) {
        val width = scene.width
        val height = scene.height

        val inputWidth = config.pipeline.inputWidth.value
        val inputHeight = config.pipeline.inputHeight.value

        var canvasWidth = width - sideBarWidth
        var canvasHeight = height

        if(inputWidth >= inputHeight) {
            val sizeFactor = canvasWidth / inputWidth.toFloat()
            canvasHeight = inputHeight * sizeFactor
        } else {
            val sizeFactor = canvasHeight / inputHeight.toFloat()
            canvasWidth = inputWidth * sizeFactor
        }

        canvas.resize(canvasWidth,  canvasHeight)
        canvas.width = canvasWidth
        canvas.height = canvasHeight
    }

    private fun adjustWindowSize() {
        canvas.resize(config.previewSize.value.width,  config.previewSize.value.height)
        canvas.width = config.previewSize.value.width
        canvas.height = config.previewSize.value.height

        stage.width = canvas.width + sideBarWidth
        stage.height = max(config.previewSize.value.height, 360.0)
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

        val scrollPane = ScrollPane(propertiesControl)
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.prefWidth = sideBarWidth
        val vb = VBox(top, scrollPane)

        //setHgrow(canvas, Priority.ALWAYS)
        setHgrow(vb, Priority.ALWAYS)
        val hb = HBox(canvas, vb)
        hb.alignment = Pos.CENTER
        return hb
    }
}