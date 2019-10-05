package ch.zhdk.tracking.model

import org.bytedeco.opencv.opencv_core.Point2d

class TactileObject {
    var id = -1
    var position : Point2d = Point2d()

    // tracking relevant
    var isAlive = true
    var lifeTime = 0

    val intensities = RingBuffer<Double>(10)
}