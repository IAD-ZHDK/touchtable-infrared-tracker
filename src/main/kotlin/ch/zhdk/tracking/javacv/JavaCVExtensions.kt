package ch.zhdk.tracking.javacv

import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Point2d
import kotlin.math.roundToInt

fun Point2d.toPoint() : Point {
    return Point(this.x().roundToInt(), this.y().roundToInt())
}