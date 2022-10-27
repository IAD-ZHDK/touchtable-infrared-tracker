package ch.zhdk.tracking.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.NumberParameter
import com.google.gson.annotations.Expose

class WebSocketConfig {
    @Expose
    @NumberParameter("Port")
    var port = DataModel(8001)
}