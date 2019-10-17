package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.model.math.Float2
import ch.bildspur.ui.properties.BooleanParameter
import ch.bildspur.ui.properties.Float2Parameter
import ch.bildspur.ui.properties.LabelParameter
import com.google.gson.annotations.Expose

class CalibrationConfig {

    @BooleanParameter("Calibration Enabled")
    var calibrationEnabled = DataModel(true)

    @BooleanParameter("Display Screen")
    var displayScreen = DataModel(true)

    @LabelParameter("Screen Edges")
    val outputLabel = ""

    @Expose
    @Float2Parameter("Top Left")
    val topLeft = DataModel(Float2())

    @Expose
    @Float2Parameter("Top Right")
    val topRight = DataModel(Float2())

    @Expose
    @Float2Parameter("Bottom Right")
    val bottomRight = DataModel(Float2())

    @Expose
    @Float2Parameter("Bottom Left")
    val bottomLeft = DataModel(Float2())
}