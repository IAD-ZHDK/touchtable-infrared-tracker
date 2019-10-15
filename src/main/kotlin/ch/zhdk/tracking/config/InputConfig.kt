package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.EnumParameter
import ch.bildspur.ui.properties.LabelParameter
import ch.bildspur.ui.properties.NumberParameter
import ch.zhdk.tracking.io.InputProviderType
import com.google.gson.annotations.Expose

class InputConfig {
    @Expose
    @EnumParameter("Input Provider")
    var inputProvider = DataModel(InputProviderType.Image)

    @LabelParameter("WebCam")
    private val webCamLabel = ""

    @Expose
    @NumberParameter("Device Index")
    var webCamDeviceIndex = DataModel(0)

    @Expose
    @NumberParameter("Input Width", "px")
    var webCamWidth = DataModel(1280)

    @Expose
    @NumberParameter("Input Height", "px")
    var webCamHeight = DataModel(720)

    @LabelParameter("RealSense")
    private val realSenseLabel = ""

    @Expose
    @NumberParameter("Device Index")
    var realSenseDeviceIndex = DataModel(0)

    @Expose
    @NumberParameter("Input Width", "px")
    var realSenseWidth = DataModel(848)

    @Expose
    @NumberParameter("Input Height", "px")
    var realSenseHeight = DataModel(480)

    @Expose
    @NumberParameter("Frame Rate", "fps")
    var realSenseFrameRate = DataModel(60)
}