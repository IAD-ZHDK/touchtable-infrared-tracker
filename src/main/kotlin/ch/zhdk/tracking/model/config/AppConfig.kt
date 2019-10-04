package ch.zhdk.tracking.model.config

import ch.zhdk.tracking.model.DataModel
import ch.zhdk.tracking.ui.properties.BooleanParameter
import com.google.gson.annotations.Expose

class AppConfig {

    @Expose
    var visual = VisualConfig()

    @Expose
    @BooleanParameter("Debugging Mode")
    var debuggingMode = DataModel(true)
}