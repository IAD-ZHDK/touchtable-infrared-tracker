package ch.zhdk.tracking.model.identification

import ch.bildspur.timer.ElapsedTimer
import ch.bildspur.util.isBetween
import ch.bildspur.util.isInMargin
import ch.zhdk.tracking.pipeline.identification.BinaryIdentifierPhase

data class Identification(var identifierPhase : BinaryIdentifierPhase = BinaryIdentifierPhase.Requested) {
    // sampling
    val samplingTimer = ElapsedTimer(0)
    val samples = ArrayList<IntensitySample>(256)

    // threshold
    var thresholdMargin = 0.0
    var stopBitThreshold = 0.0
    var lowThreshold = 0.0
    var highThreshold = 0.0

    fun getFlank(sample : IntensitySample) : Flank {
        return Flank(getFlankType(sample.intensity), sample.timestamp)
    }

    fun getFlankType(value : Double) : FlankType {
        if(value.isInMargin(highThreshold, thresholdMargin))
            return FlankType.High

        if(value.isInMargin(lowThreshold, thresholdMargin))
            return FlankType.Low

        if(value.isInMargin(stopBitThreshold, thresholdMargin))
            return FlankType.Stop

        return FlankType.OutOfRange
    }
}