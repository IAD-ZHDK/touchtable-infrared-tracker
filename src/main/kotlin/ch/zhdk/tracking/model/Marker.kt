package ch.zhdk.tracking.model

import ch.zhdk.tracking.model.identification.Identification
import org.bytedeco.opencv.opencv_core.Point2d

class Marker(uniqueId: Int) : TrackingEntity(uniqueId) {
    var identifier = -1
    var position = Point2d()
    var rotation = 0.0

    // todo: maybe just calculate on the fly
    var normalizedPosition = Point2d()
    var normalizedIntensity: Double = 0.0
    var calibratedPosition = Point2d()

    // tracking relevant
    var matchedWithRegion = false

    // intensity detected by active region
    var intensity = 0.0

    // identification
    // todo: move this property into tactile object
    val identification = Identification()
}