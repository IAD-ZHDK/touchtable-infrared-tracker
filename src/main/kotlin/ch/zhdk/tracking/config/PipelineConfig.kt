package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.*
import ch.zhdk.tracking.pipeline.PipelineType
import ch.zhdk.tracking.pipeline.detection.BinarizationMethod
import com.google.gson.annotations.Expose

class PipelineConfig {
    @Expose
    @GroupParameter("Calibration", false)
    var calibration = CalibrationConfig()

    @Expose
    @EnumParameter("Pipeline")
    var pipelineType = DataModel(PipelineType.Simple)

    @BooleanParameter("Processing Enabled")
    var enabled = DataModel(true)

    @Expose
    @BooleanParameter("Annotate Output")
    var annotateOutput = DataModel(true)

    @Expose
    @BooleanParameter("Display Output")
    var displayOutput = DataModel(true)

    @LabelParameter("Information")
    val informationLabel = ""

    @StringParameter("Input Width", isEditable = false)
    var inputWidth = DataModel(1)

    @StringParameter("Input Height", isEditable = false)
    var inputHeight = DataModel(1)

    @StringParameter("Unique Marker Id", isEditable = false)
    var uniqueMarkerId = DataModel(0)

    @StringParameter("Unique Tactile Object Id", isEditable = false)
    var uniqueTactileObjectId = DataModel(0)

    @StringParameter("Detected Objects", isEditable = false)
    var actualObjectCount = DataModel(0)

    @StringParameter("Frame Time", isEditable = false)
    var frameTime = DataModel("- ms")

    @StringParameter("Processing Time", isEditable = false)
    var processingTime = DataModel("- ms")

    @ActionParameter("Unique Id", "Reset")
    private var resetUniqueId = {
        uniqueMarkerId.value = 0
    }

    @LabelParameter("Pre-Processing")
    private val inputLabel = ""

    @Expose
    @BooleanParameter("Enable Pre-Processing")
    var enablePreProcessing = DataModel(true)

    @Expose
    @SliderParameter("Gamma Correction", 0.04, 4.0, 0.01)
    var gammaCorrection = DataModel(1.0)

    @LabelParameter("Detection")
    val detectionLabel = ""

    @Expose
    @EnumParameter("Binarization Method")
    var binarizationMethod = DataModel(BinarizationMethod.Adaptive)

    @Expose
    @SliderParameter("Threshold", 1.0, 100.0, 0.05, snap = true)
    var threshold = DataModel(50.0)

    @Expose
    @SliderParameter("Adaptiveness", -20.0, 20.0, 0.01, snap = true)
    var adaptiveness = DataModel(-5.0)

    @Expose
    @BooleanParameter("Morphology Filter")
    var morphologyFilterEnabled = DataModel(false)

    @Expose
    @SliderParameter("Erode Size", 1.0, 10.0, 1.0, snap = true)
    var erodeSize = DataModel(4)

    @Expose
    @SliderParameter("Dilate Size", 1.0, 10.0, 1.0, snap = true)
    var dilateSize = DataModel(4)

    @Expose
    @BooleanParameter("Detect Simple Orientation")
    var detectSimpleOrientation = DataModel(false)

    @Expose
    @SliderParameter("Min Area Size (px)", 0.0, 100.0, 1.0, snap = true)
    var minAreaSize = DataModel(5.0)

    @LabelParameter("Tracking")
    val trackingLabel = ""

    @Expose
    @SliderParameter("Max Delta", 0.0, 200.0, 1.0)
    var markerMaxDelta = DataModel(40.0)

    @Expose
    @NumberParameter("Min Detected Time", "ms")
    var minDetectedTime = DataModel(250)

    @Expose
    @NumberParameter("Max Missing Time", "ms")
    var maxMissingTime = DataModel(250)

    @LabelParameter("Clustering")
    val clusteringLabel = ""

    @Expose
    @SliderParameter("Max Radius", 0.0, 200.0, 1.0)
    var maximumRadius = DataModel(15.0)

    @Expose
    @SliderParameter("Devices Max Delta", 0.0, 200.0, 1.0)
    var deviceMaxDelta = DataModel(80.0)

    @LabelParameter("Identification")
    private val identificationLabel = ""

    @Expose
    @BooleanParameter("Identification Enabled")
    var identificationEnabled = DataModel(true)

    @Expose
    @GroupParameter("BluetoothLE", expanded = false)
    var bleConfig = BLEConfig()

    @Expose
    @SliderParameter("Samples", 0.0, 300.0, 1.0, snap = true)
    // todo: could be calculated from frame rate and protocol speed
    var sampleCount = DataModel(90)

    @Expose
    @SliderParameter("Threshold Margin", 0.0, 1.0, 0.05, snap = true)
    var thresholdMarginFactor = DataModel(1.0)
}