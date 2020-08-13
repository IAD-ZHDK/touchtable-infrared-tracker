package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.math.Float2
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.TrackingApplication
import com.google.gson.annotations.Expose

class CalibrationConfig {

    @Expose
    @BooleanParameter("Calibration Enabled")
    var calibrationEnabled = DataModel(true)

    @Expose
    @BooleanParameter("Display Annotation")
    var displayAnnotation = DataModel(true)

    @Expose
    @BooleanParameter("Use Perspective Transform")
    var perspectiveTransform = DataModel(false)

    @LabelParameter("Calibration")
    private val calibrationLabel = ""

    @StringParameter("Instruction", isEditable = false)
    private var instruction = DataModel("")

    @ActionParameter("Edge Selection", "Start")
    private val selectEdgesAction = {
        instruction.value = "Press top left..."
        topLeft.value = TrackingApplication.requestMousePressed()

        if(perspectiveTransform.value) {
            instruction.value = "Press top right..."
            topRight.value = TrackingApplication.requestMousePressed()
        }

        instruction.value = "Press bottom right..."
        bottomRight.value = TrackingApplication.requestMousePressed()

        if(perspectiveTransform.value) {
            instruction.value = "Press bottom left..."
            bottomLeft.value = TrackingApplication.requestMousePressed()
        }

        instruction.value = "Calibration finished"
    }

    @LabelParameter("Screen Edges")
    private val edgeLabel = ""

    @Expose
    @Float2Parameter("Top Left")
    var topLeft = DataModel(Float2(0f, 0f))

    @Expose
    @Float2Parameter("Top Right")
    var topRight = DataModel(Float2(1f, 0f))

    @Expose
    @Float2Parameter("Bottom Right")
    var bottomRight = DataModel(Float2(1f, 1f))

    @Expose
    @Float2Parameter("Bottom Left")
    var bottomLeft = DataModel(Float2(0f, 1f))
}