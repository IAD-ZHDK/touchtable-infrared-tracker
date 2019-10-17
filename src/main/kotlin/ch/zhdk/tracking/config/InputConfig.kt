package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.CVPreview
import ch.zhdk.tracking.io.InputProviderType
import com.google.gson.annotations.Expose

class InputConfig {
    @ActionParameter("Pipeline", "Restart")
    private val restartPipeline = {
        CVPreview.restartRequested = true
    }

    @LabelParameter("Input")
    val inputLabel = ""

    @Expose
    @EnumParameter("Input Provider")
    var inputProvider = DataModel(InputProviderType.Image)

    @LabelParameter("CameraInput")
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

    @LabelParameter("RealSense2")
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