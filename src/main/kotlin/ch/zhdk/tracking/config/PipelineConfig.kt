package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.BooleanParameter
import ch.bildspur.ui.properties.EnumParameter
import ch.bildspur.ui.properties.LabelParameter
import ch.bildspur.ui.properties.SliderParameter
import ch.zhdk.tracking.pipeline.PipelineType
import com.google.gson.annotations.Expose

class PipelineConfig {
    @Expose
    @EnumParameter("Pipeline")
    var pipelineType = DataModel(PipelineType.Simple)

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
}