package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.pipeline.PipelineType
import com.google.gson.annotations.Expose

class PipelineConfig {
    @Expose
    @EnumParameter("Pipeline")
    var pipelineType = DataModel(PipelineType.Simple)

    @Expose
    @BooleanParameter("Annotate Output")
    var annotateOutput = DataModel(true)

    @LabelParameter("Information")
    val informationLabel = ""

    @StringParameter("Input Width", isEditable = false)
    var inputWidth = DataModel(1)

    @StringParameter("Input Height", isEditable = false)
    var inputHeight = DataModel(1)

    @StringParameter("Total Detected Objects", isEditable = false)
    var uniqueId = DataModel(0)

    @StringParameter("Detected Objects", isEditable = false)
    var actualObjectCount = DataModel(0)

    @StringParameter("Frame Time", isEditable = false)
    var frameTime = DataModel("- ms")

    @StringParameter("Processing Time", isEditable = false)
    var processingTime = DataModel("- ms")

    @LabelParameter("Detection")
    val detectionLabel = ""

    @Expose
    @SliderParameter("Threshold", 0.0, 255.0, 1.0)
    var threshold = DataModel(200.0)

    @Expose
    @BooleanParameter("Morphology Filter")
    var morphologyFilterEnabled = DataModel(true)

    @Expose
    @SliderParameter("Erode Size", 1.0, 10.0, 1.0, snap = true)
    var erodeSize = DataModel(2)

    @Expose
    @SliderParameter("Dilate Size", 1.0, 10.0, 1.0, snap = true)
    var dilateSize = DataModel(2)

    @Expose
    @BooleanParameter("Detect Orientation")
    var detectOrientation = DataModel(true)

    @Expose
    @SliderParameter("Approximation Level", 0.1, 10.0, 0.1, snap = true)
    var approximationEpsilon = DataModel(3.0)

    @LabelParameter("Tracking")
    val trackingLabel = ""

    @Expose
    @SliderParameter("Max Delta", 0.0, 200.0, 1.0)
    var maxDelta = DataModel(40.0)

    @LabelParameter("Identification")
    val identificationLabel = ""

    @Expose
    @NumberParameter("Sampling Time", "ms")
    var samplingTime = DataModel(1200L)

    @Expose
    @SliderParameter("Threshold Margin", 0.0, 1.0, 0.05, snap = true)
    var thresholdMarginFactor = DataModel(0.75)
}