package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.TrackingApplication
import ch.zhdk.tracking.ui.PreviewSize
import com.google.gson.annotations.Expose

class AppConfig {
    @Expose
    var input = InputConfig()

    @Expose
    var pipeline = PipelineConfig()

    @Expose
    var output = OutputConfig()

    @LabelParameter("Preview")
    private val outputLabel = ""

    @Expose
    @BooleanParameter("Production Mode")
    var productionMode = DataModel(false)

    @Expose
    @BooleanParameter("Display Processed")
    var displayProcessed = DataModel(true)

    @Expose
    @EnumParameter("Preview Width")
    var previewSize = DataModel(PreviewSize.HDReady)

    @ActionParameter("Input Frame", "Save", false)
    private val requestScreenshot = {
        TrackingApplication.saveFrameRequested = true
    }

    @ActionParameter("Pipeline", "Restart")
    private val restartPipeline = {
        TrackingApplication.requestPipelineRestart(true)
    }

    @LabelParameter("Information")
    private val infoLabel = ""

    @StringParameter("Message", isEditable = false)
    var message = DataModel("")

    @TextParameter("Error", isEditable = false, wordWrap = true)
    var errorMessage = DataModel("")
}