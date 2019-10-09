package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.NumberParameter
import ch.bildspur.ui.properties.StringParameter
import com.google.gson.annotations.Expose

class OutputConfig {
    @Expose
    @StringParameter("OSC Address")
    var oscAddress = DataModel("192.168.1.255")

    @Expose
    @NumberParameter("OSC Port")
    var oscPort = DataModel(8002)

    @Expose
    @NumberParameter("Update Frequency", " FPS")
    var updateFrequency = DataModel(20)
}