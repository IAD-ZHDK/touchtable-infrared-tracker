package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.CVPreview
import com.google.gson.annotations.Expose

class AppConfig {
    @Expose
    var input = InputConfig()

    @Expose
    var pipeline = PipelineConfig()

    @Expose
    var osc = OscConfig()

    @Expose
    @BooleanParameter("Debugging Mode")
    var debuggingMode = DataModel(true)

    @LabelParameter("Preview")
    private val outputLabel = ""

    @Expose
    @BooleanParameter("Display Processed")
    var displayProcessed = DataModel(true)

    @Expose
    @NumberParameter("Preview Update Frequency", " FPS")
    var updateFrequency = DataModel(90)

    @ActionParameter("Input Frame", "Save", false)
    private val requestScreenshot = {
        CVPreview.saveFrameRequested = true
    }

    @ActionParameter("Pipeline", "Restart")
    private val restartPipeline = {
        CVPreview.restartRequested = true
    }

    @LabelParameter("Information")
    private val infoLabel = ""

    @StringParameter("Message", isEditable = false)
    var message = DataModel("")

    @TextParameter("Error", isEditable = false, wordWrap = true)
    var errorMessage = DataModel("")
}