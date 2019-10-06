package ch.zhdk.tracking.model

import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.pipeline.identification.BinaryIdentifierPhase

data class Identification(var identifierPhase : BinaryIdentifierPhase = BinaryIdentifierPhase.Requested) {
    // sampling
    val samplingTimer = ElapsedTimer(0)
    val intensities = ArrayList<Double>(256)

    // threshold
    var thresholdMargin = 0.0
    var stopBitThreshold = 0.0
    var lowThreshold = 0.0
    var highThreshold = 0.0
}