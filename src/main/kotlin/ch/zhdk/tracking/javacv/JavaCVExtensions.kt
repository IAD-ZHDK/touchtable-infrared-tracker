package ch.zhdk.tracking.javacv

import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Point2d
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Point2d.toPoint() : Point {
    return Point(this.x().roundToInt(), this.y().roundToInt())
}

fun Point2d.distance(p2: Point2d): Double {
    return sqrt((p2.x() - this.x()).pow(2.0) + (p2.y() - this.y()).pow(2.0))
}