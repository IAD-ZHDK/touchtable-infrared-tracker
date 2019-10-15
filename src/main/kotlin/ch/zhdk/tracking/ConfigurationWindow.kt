package ch.zhdk.tracking

import ch.bildspur.configuration.ConfigurationController
import ch.zhdk.tracking.config.AppConfig
import ch.bildspur.ui.properties.PropertiesControl
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.stage.Screen
import javafx.stage.Stage

class ConfigWindow(val configController : ConfigurationController, val config : AppConfig) : Application() {
    private val windowName = "ZHdK - IR Tracker"
    private val propertiesControl = PropertiesControl()

    override fun start(primaryStage: Stage) {
        primaryStage.title = windowName

        val root = createUI(primaryStage)
        primaryStage.scene = Scene(root, 420.0, 600.0)
        primaryStage.setOnShown {
            propertiesControl.resize(primaryStage.scene.width, primaryStage.scene.height)
        }

        primaryStage.setOnCloseRequest {
            CVPreview.running = false
        }

        val primScreenBounds = Screen.getPrimary().visualBounds
        primaryStage.x = primScreenBounds.width / 8.0 * 7.0

        primaryStage.show()
    }

    private fun createUI(primaryStage: Stage) : Pane {
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

        val settings = mapOf(
            "App" to config,
            "Pipeline" to config.pipeline,
            "Input" to config.input,
            "Output" to config.output,
            "Visual" to config.visual
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
        return BorderPane(ScrollPane(propertiesControl), top, null, null, null)
    }
}