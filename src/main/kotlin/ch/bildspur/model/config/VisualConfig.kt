package ch.bildspur.model.config

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.BooleanParameter
import ch.bildspur.ui.properties.NumberParameter
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