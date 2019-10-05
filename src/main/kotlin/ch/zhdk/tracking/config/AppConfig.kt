package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.model.config.VisualConfig
import ch.bildspur.ui.properties.BooleanParameter
import ch.bildspur.ui.properties.LabelParameter
import com.google.gson.annotations.Expose

class AppConfig {

    @Expose
    var visual = VisualConfig()

    @Expose
    var inputConfig = InputConfig()

    @Expose
    var pipeline = PipelineConfig()

    @Expose
    @BooleanParameter("Debugging Mode")
    var debuggingMode = DataModel(true)

    @LabelParameter("Output")
    val outputLabel = ""

    @Expose
    @BooleanParameter("Display Processed")
    var displayProcessed = DataModel(true)
}