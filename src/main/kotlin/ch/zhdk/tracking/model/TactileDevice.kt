package ch.zhdk.tracking.model

import ch.zhdk.tracking.model.identification.Identification
import org.bytedeco.opencv.opencv_core.Point2d


class TactileDevice(uniqueId : Int) : TrackingEntity(uniqueId) {
    var normalizedPosition = Point2d()
    var normalizedIntensity: Double = 0.0
    var calibratedPosition = Point2d()

    val identification = Identification()
    var identifier = -1

    var rotation = 0.0

    var intensity = 0.0

    val markers = ArrayList<Marker>(4)
}