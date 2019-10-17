package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.model.math.Float2
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.CVPreview
import com.google.gson.annotations.Expose
import kotlin.concurrent.thread

class CalibrationConfig {

    @BooleanParameter("Calibration Enabled")
    var calibrationEnabled = DataModel(false)

    @BooleanParameter("Display Screen")
    var displayScreen = DataModel(true)

    @LabelParameter("Calibration")
    private val calibrationLabel = ""

    @StringParameter("Instruction", isEditable = false)
    private var instruction = DataModel("")

    @ActionParameter("Edge Selection", "Start")
    private val selectEdgesAction = {
        thread {
            instruction.value = "Press top left..."
            topLeft.value = CVPreview.requestMousePressed()

            instruction.value = "Press top right..."
            topRight.value = CVPreview.requestMousePressed()

            instruction.value = "Press bottom right..."
            bottomRight.value = CVPreview.requestMousePressed()

            instruction.value = "Press bottom left..."
            bottomLeft.value = CVPreview.requestMousePressed()

            instruction.value = "Calibration finished"
        }
    }

    @LabelParameter("Screen Edges")
    private val edgeLabel = ""

    @Expose
    @Float2Parameter("Top Left")
    var topLeft = DataModel(Float2())

    @Expose
    @Float2Parameter("Top Right")
    var topRight = DataModel(Float2())

    @Expose
    @Float2Parameter("Bottom Right")
    var bottomRight = DataModel(Float2())

    @Expose
    @Float2Parameter("Bottom Left")
    var bottomLeft = DataModel(Float2())
}