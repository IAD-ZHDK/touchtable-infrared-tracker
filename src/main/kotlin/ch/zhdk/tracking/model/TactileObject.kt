package ch.zhdk.tracking.model

import ch.bildspur.model.RingBuffer
import org.bytedeco.opencv.opencv_core.Point2d

class TactileObject(val uniqueId : Int) {
    var identifier = -1
    var position : Point2d = Point2d()

    // tracking relevant
    var isAlive = true
    var lifeTime = 0

    val intensities = RingBuffer<Double>(10)
}