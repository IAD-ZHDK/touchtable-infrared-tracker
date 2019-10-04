package ch.zhdk.tracking.model.config

import ch.zhdk.tracking.model.DataModel
import ch.zhdk.tracking.ui.properties.BooleanParameter
import ch.zhdk.tracking.ui.properties.NumberParameter
import com.google.gson.annotations.Expose

class VisualConfig {
    @Expose
    @BooleanParameter("Fullscreen")
    var fullScreen = DataModel(false)

    @Expose
    @NumberParameter("Screen Index")
    var screenIndex = DataModel(0)

    @Expose
    @NumberParameter("Width")
    var width = DataModel(1024)

    @Expose
    @NumberParameter("Height")
    var height = DataModel(768)

    @Expose
    @NumberParameter("FrameRate")
    var frameRate = DataModel(60)

    @Expose
    @NumberParameter("Pixel Denstiy")
    var pixelDensity = DataModel(2)
}