package ch.bildspur.model.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.BooleanParameter
import com.google.gson.annotations.Expose

class AppConfig {

    @Expose
    var visual = VisualConfig()

    @Expose
    @BooleanParameter("Debugging Mode")
    var debuggingMode = DataModel(true)
}