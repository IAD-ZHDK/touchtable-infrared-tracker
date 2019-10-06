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

    // intensity detected by active region
    var intensity = 0.0

    // last updated by active region (frame timestamp)
    var timestamp = 0L

    // identification
    val identification = Identification()
}