package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.SliderParameter
import com.google.gson.annotations.Expose

class PipelineConfig {

    @Expose
    @SliderParameter("Threshold", 0.0, 255.0, 1.0)
    var threshold = DataModel(125.0)
}