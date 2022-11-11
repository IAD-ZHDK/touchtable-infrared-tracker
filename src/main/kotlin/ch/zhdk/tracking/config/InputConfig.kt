package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.TrackingApplication
import ch.zhdk.tracking.io.InputProviderType
import com.google.gson.annotations.Expose
import java.nio.file.Paths

class InputConfig {
    @ActionParameter("Pipeline", "Restart")
    private val restartPipeline = {
        TrackingApplication.requestPipelineRestart(true)
    }

    @LabelParameter("Input")
    val inputLabel = ""

    @Expose
    @EnumParameter("Input Provider")
    var inputProvider = DataModel(InputProviderType.Interactive)

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
    var realSenseFrameRate = DataModel(30)

    @Expose
    @BooleanParameter("Enable RGB IR Stream")
    var enableRGBIRStream = DataModel(false)

    @Expose
    @BooleanParameter("Enable Dual-IR Stream")
    var enableDualIR = DataModel(false)

    @Expose
    @BooleanParameter("Display Second IR Stream")
    var displaySecondIRStream = DataModel(false)

    @Expose
    @BooleanParameter("Enable Auto White Balance")
    var enableAutoWhiteBalance = DataModel(true)

    @Expose
    @BooleanParameter("Enable Auto Exposure")
    var enableAutoExposure = DataModel(true)

    @Expose
    @SliderParameter("Exposure", 0.0, 50000.0, 500.0, snap = true)
    var autoExposure = DataModel(33000f)

    @Expose
    @BooleanParameter("Enable IR Emitter")
    var enableIREmitter = DataModel(false)

    @LabelParameter("Video Input")
    private val videoLabel = ""

    @Expose
    @PathParameter("Video File", extensions = ["*.mp4", "*.mov"])
    var videoFile = DataModel(Paths.get(""))

    @LabelParameter("Image Input")
    private val imageLabel = ""

    @Expose
    @PathParameter("Image File", extensions = ["*.png", "*.jpeg", "*.jpg", "*.bmp", "*.gif"])
    var imageFile = DataModel(Paths.get(""))
}