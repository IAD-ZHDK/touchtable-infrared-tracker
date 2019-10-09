package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.NumberParameter
import com.google.gson.annotations.Expose

class OutputConfig {
    @Expose
    @NumberParameter("OSC Port")
    var oscPort = DataModel(8002)

    @Expose
    @NumberParameter("Update Frequency", " FPS")
    var updateFrequency = DataModel(30)
}