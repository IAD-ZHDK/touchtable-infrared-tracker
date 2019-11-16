package ch.zhdk.tracking.model

import averageBy
import ch.zhdk.tracking.model.identification.Identification
import org.bytedeco.opencv.opencv_core.Point2d


class TactileDevice(uniqueId : Int) : TrackingEntity(uniqueId) {
    var position = Point2d()

    var normalizedPosition = Point2d()
    var normalizedIntensity: Double = 0.0
    var calibratedPosition = Point2d()

    val identification = Identification()
    var identifier = -1

    var rotation = 0.0

    var intensity = 0.0

    var markers = ArrayList<Marker>(4)

    var matchedWithCentroid = false

    fun update() {
        if(markers.size < 3)
            return

        updatePosition()
        updateRotation()
    }

    private fun updatePosition() {
        var x = 0.0
        var y = 0.0

        markers.forEach {
            x += it.position.x()
            y += it.position.y()
        }

        x /= markers.size
        y /= markers.size

        position.x(x)
        position.y(y)
    }

    private fun updateRotation() {

    }
}