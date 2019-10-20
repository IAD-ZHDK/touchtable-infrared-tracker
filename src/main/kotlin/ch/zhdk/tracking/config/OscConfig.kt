package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.ActionParameter
import ch.bildspur.ui.properties.BooleanParameter
import ch.bildspur.ui.properties.NumberParameter
import ch.bildspur.ui.properties.StringParameter
import ch.zhdk.tracking.TrackingApplication
import com.google.gson.annotations.Expose

class OscConfig {
    @Expose
    @StringParameter("OSC Address")
    var oscAddress = DataModel("127.0.0.1")

    @Expose
    @NumberParameter("OSC Port")
    var oscPort = DataModel(8002)

    @ActionParameter("OSC", "Restart")
    private val restartOutput = {
        TrackingApplication.initOSC()
    }

    @Expose
    @StringParameter("Namespace", isEditable = false)
    var nameSpace = DataModel("/ir-tracker")

    @Expose
    @NumberParameter("Update Frequency", " FPS")
    var updateFrequency = DataModel(20)

    @Expose
    @BooleanParameter("Debug OSC")
    var debugOSC = DataModel(false)
}