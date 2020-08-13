package ch.zhdk.tracking.config

import ch.bildspur.ui.properties.GroupParameter
import com.google.gson.annotations.Expose

class OutputConfig {
    @Expose
    @GroupParameter("Open Sound Control")
    var osc = OscConfig()
}