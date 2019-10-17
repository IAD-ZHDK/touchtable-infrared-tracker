package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.BooleanParameter
import ch.bildspur.ui.properties.LabelParameter
import com.google.gson.annotations.Expose

class CalibrationConfig {

    @BooleanParameter("Calibration Enabled")
    var calibrationEnabled = DataModel(true)

    @LabelParameter("Screen Edges")
    val outputLabel = ""

    
}