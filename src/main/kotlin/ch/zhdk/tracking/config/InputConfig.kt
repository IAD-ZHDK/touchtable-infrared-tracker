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

    @LabelParameter("RealSense")
    private val cameraLabel = ""

    @Expose
    @NumberParameter("Input Width", "px")
    var inputWidth = DataModel(848)

    @Expose
    @NumberParameter("Input Height", "px")
    var inputHeight = DataModel(480)

    @Expose
    @NumberParameter("Frame Rate", "fps")
    var inputFrameRate = DataModel(60)

    @Expose
    @NumberParameter("Device Index")
    var deviceIndex = DataModel(0)
}