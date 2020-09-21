package ch.zhdk.tracking.ui

import ch.bildspur.math.Float2
import ch.bildspur.model.DataModel
import ch.zhdk.tracking.TrackingApplication
import ch.zhdk.tracking.config.CalibrationConfig
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class CalibrationWindow(val config: CalibrationConfig) {

    class Measurement(val name : String, val probe : Float2, val model : DataModel<Float2>) {
        var measured = Float2()
    }

    val offset = 0.1f

    val mutex = Semaphore(0)
    var finished = AtomicBoolean(false)

    val window = Stage()
    val nextButton = Button("Confirm Position")
    val cancelButton = Button("Cancel")

    // use screen dpi for circle size
    val circle = Circle(120.0, Color.AQUAMARINE)

    val measurements = listOf(
        Measurement("Center", Float2(0.5f, 0.5f), DataModel(Float2())),
        Measurement("Top Left", Float2(offset, offset), config.topLeft),
        Measurement("Top Right", Float2(1.0f - offset, offset), config.topRight),
        Measurement("Bottom Right", Float2(1.0f - offset, 1.0f - offset), config.bottomRight),
        Measurement("Bottom Left", Float2(offset, 1.0f - offset), config.bottomLeft)
    )

    fun show() {
        val pane = Pane()
        pane.style = "-fx-background-color: DIMGRAY;"

        val scene = Scene(pane)

        // create window
        window.title = "Calibration Wizard"
        window.scene = scene

        window.initStyle(StageStyle.UNDECORATED)
        window.isAlwaysOnTop = true

        // make full screen
        val screenBounds = Screen.getPrimary().bounds
        window.x = 0.0
        window.y = 0.0
        window.width = screenBounds.width
        window.height = screenBounds.height

        // setup cancel button
        cancelButton.prefWidth = 60.0
        cancelButton.prefHeight = 28.0
        cancelButton.layoutX = (window.width * 0.5) - (cancelButton.prefWidth * 0.5)
        cancelButton.layoutY = window.height * 0.90 - (cancelButton.prefHeight * 0.5)

        cancelButton.setOnAction {
            window.close()
        }

        // setup next button
        nextButton.prefWidth = 200.0
        nextButton.prefHeight = 80.0

        nextButton.layoutX = (window.width * 0.5) - (nextButton.prefWidth * 0.5)
        nextButton.layoutY = window.height * 0.75 - (nextButton.prefHeight * 0.5)
        nextButton.style = "-fx-font-weight: bold;"

        nextButton.setOnAction {
            if (finished.get())
                window.close()
            mutex.release()
        }

        // add circle
        // todo: make circle more visible (use multiple circles)
        circle.stroke = Color.AQUAMARINE
        circle.fill = Color.TRANSPARENT
        circle.strokeDashArray.addAll(2.0, 21.0)
        circle.strokeWidth = 3.0

        pane.children.add(nextButton)
        pane.children.add(cancelButton)
        pane.children.add(circle)
        window.show()

        thread {
            runWizard()
        }
    }

    private fun runWizard() {
        // capture measurements
        for (measurement in measurements) {
            Platform.runLater {
                // set position
                circle.centerX = window.width * measurement.probe.x
                circle.centerY = window.height * measurement.probe.y
            }

            // wait for button press and active tactile device
            do {
                mutex.acquire()
            } while (TrackingApplication.pipeline.devices.size != 1)

            // store position
            val position = TrackingApplication.pipeline.devices.first().position
            measurement.measured.x = position.x().toFloat()
            measurement.measured.y = position.y().toFloat()
        }

        // calculate calibration positions
        val center = measurements.first().measured

        for(measurement in measurements.takeLast(measurements.size - 1)) {
            var direction = measurement.measured - center

            // increase size
            direction /= 1.0f - offset

            val position = direction + center

            measurement.model.value.x = position.x / TrackingApplication.pipeline.inputFrame.width.toFloat()
            measurement.model.value.y = position.y / TrackingApplication.pipeline.inputFrame.height.toFloat()
        }

        // finish calibration
        Platform.runLater {
            window.close()
        }
    }
}