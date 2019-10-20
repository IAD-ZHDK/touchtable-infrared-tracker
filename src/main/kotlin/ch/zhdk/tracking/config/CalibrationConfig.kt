package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.model.math.Float2
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.TrackingApplication
import com.google.gson.annotations.Expose

class CalibrationConfig {

    @BooleanParameter("Calibration Enabled")
    var calibrationEnabled = DataModel(true)

    @BooleanParameter("Display Annotation")
    var displayAnnotation = DataModel(true)

    @LabelParameter("Calibration")
    private val calibrationLabel = ""

    @StringParameter("Instruction", isEditable = false)
    private var instruction = DataModel("")

    @ActionParameter("Edge Selection", "Start")
    private val selectEdgesAction = {
        instruction.value = "Press top left..."
        topLeft.value = TrackingApplication.requestMousePressed()

        // todo: re-enable this
        instruction.value = "Press top right..."
        //topRight.value = CVPreview.requestMousePressed()

        instruction.value = "Press bottom right..."
        bottomRight.value = TrackingApplication.requestMousePressed()

        instruction.value = "Press bottom left..."
        //bottomLeft.value = CVPreview.requestMousePressed()

        instruction.value = "Calibration finished"
    }

    @LabelParameter("Screen Edges")
    private val edgeLabel = ""

    @Expose
    @Float2Parameter("Top Left")
    var topLeft = DataModel(Float2(0f, 0f))

//    @Expose
//    @Float2Parameter("Top Right")
//    var topRight = DataModel(Float2(1f, 0f))

    @Expose
    @Float2Parameter("Bottom Right")
    var bottomRight = DataModel(Float2(1f, 1f))

//    @Expose
//    @Float2Parameter("Bottom Left")
//    var bottomLeft = DataModel(Float2(0f, 1f))
}