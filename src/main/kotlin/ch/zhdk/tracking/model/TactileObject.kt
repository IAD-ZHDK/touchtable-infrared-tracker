package ch.zhdk.tracking.model

import ch.zhdk.tracking.model.identification.Identification
import org.bytedeco.opencv.opencv_core.Point2d

class TactileObject(val uniqueId: Int) {
    var identifier = -1
    var position = Point2d()
    var rotation = 0.0

    var normalizedPosition = Point2d()
    var normalizedIntensity: Double = 0.0
    var calibratedPosition = Point2d()

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