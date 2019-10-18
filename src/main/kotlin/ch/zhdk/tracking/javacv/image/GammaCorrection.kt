package ch.zhdk.tracking.javacv.image

import org.bytedeco.opencv.global.opencv_core.CV_8U
import org.bytedeco.opencv.global.opencv_core.LUT
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.pow
import kotlin.math.roundToInt


class GammaCorrection(private var gamma : Double = 0.0) {
    private val lookUpTable = Mat(1, 256, CV_8U)

    init {
        initLookupTable()
    }

    @Synchronized
    fun initLookupTable() {
        val lookUpTableData = ByteArray((lookUpTable.total() * lookUpTable.channels()).toInt())
        for (i in 0 until lookUpTable.cols()) {
            lookUpTableData[i] = saturate((i / 255.0).pow(gamma) * 255.0)
        }
        lookUpTable.data().put(lookUpTableData,0, lookUpTableData.size)
    }

    private fun saturate(value : Double): Byte {
        var iVal = value.roundToInt()
        iVal = if (iVal > 255) 255 else if (iVal < 0) 0 else iVal
        return iVal.toByte()
    }

    @Synchronized
    fun correct(image : Mat, gamma: Double) {
        // check if looktable is valid
        if(this.gamma != gamma) {
            println("resetting lookup table...")
            this.gamma = gamma
            initLookupTable()
        }

        // correct
        LUT(image, lookUpTable, image)
    }
}