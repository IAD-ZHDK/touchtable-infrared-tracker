package ch.zhdk.tracking.model

import ch.zhdk.tracking.javacv.normalize
import org.bytedeco.opencv.opencv_core.*

data class ActiveRegion(val center : Point2d,
                        val position : Point,
                        val size : Size,
                        val area : Double,
                        val timestamp : Long,

                        // match related
                        var matched : Boolean = false,

                        // orientation detection (will be removed)
                        var polygon : Mat = Mat(),

                        // normalization values
                        var normalizedArea : Double = 0.0,
                        var normalizedSize : Size2d = Size2d(),
                        var normalizedPosition : Point2d = Point2d(),
                        var normalizedCenter : Point2d = Point2d(),
                        var normalized : Boolean = false) {

    fun normalize(width : Double, height : Double) {
        normalizedArea = area / (width * height)

        normalizedSize.width(size.width() / width)
        normalizedSize.height(size.height() / height)

        normalizedPosition.x(position.x() / width)
        normalizedPosition.y(position.y() / height)

        normalizedCenter.x(center.x() / width)
        normalizedCenter.y(center.y() / height)

        normalized = true
    }
}