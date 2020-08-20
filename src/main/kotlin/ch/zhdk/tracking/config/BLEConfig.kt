package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.EnumParameter
import ch.bildspur.ui.properties.LabelParameter
import ch.bildspur.ui.properties.NumberParameter
import ch.bildspur.ui.properties.StringParameter
import ch.zhdk.tracking.io.BaudRate
import com.google.gson.annotations.Expose

class BLEConfig {
    @LabelParameter("Serial")
    val serialLabel = ""

    @Expose
    @StringParameter("COM Port")
    var port = DataModel("/dev/tty.usbserial-14410")

    @Expose
    @EnumParameter("Baud Rate")
    var baudRate = DataModel(BaudRate.B115200)

    @LabelParameter("Identification")
    val identificationLabel = ""

    @Expose
    @NumberParameter("Scan Interval", "s")
    var scanInterval = DataModel(10)

    @Expose
    @NumberParameter("Scan Time", "s")
    var scanTime = DataModel(2)
}