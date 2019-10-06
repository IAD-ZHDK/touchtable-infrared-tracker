package ch.zhdk.tracking.model

import ch.bildspur.model.RingBuffer
import ch.bildspur.timer.ElapsedTimer
import ch.zhdk.tracking.pipeline.identification.BinaryIdentifierPhase
import org.bytedeco.opencv.opencv_core.Point2d

class TactileObject(val uniqueId : Int) {
    var identifier = -1
    var position : Point2d = Point2d()
    lateinit var normalizedPosition : Point2d

    // tracking relevant
    var isAlive = true
    var lifeTime = 0

    // identification relevant
    var currentIntensity = 0.0
    var lastUpdateTimestamp = 0L

    // binary detection
    var identifierPhase = BinaryIdentifierPhase.Sampling
    val identifierTimer = ElapsedTimer(0)
    val intensities = RingBuffer<Double>(25) // todo: find better value then magic number

    // threshold
    var thresholdMargin = 0.0
    var stopBitThreshold = 0.0
    var lowThreshold = 0.0
    var highThreshold = 0.0

}