package ch.zhdk.tracking.javacv

import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Point2d
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_core.Size2d
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Point.toPoint2d() : Point2d {
    return Point2d(this.x().toDouble(), this.y().toDouble())
}

fun Point2d.toPoint() : Point {
    return Point(this.x().roundToInt(), this.y().roundToInt())
}

fun Point2d.distance(p2: Point2d): Double {
    return sqrt((p2.x() - this.x()).pow(2.0) + (p2.y() - this.y()).pow(2.0))
}

fun Point2d.normalize(width : Double, height : Double) {
    this.x(this.x() / width)
    this.y(this.y() / height)
}

fun Point.normalize(width : Double, height : Double) {
    this.x((this.x() / width).roundToInt())
    this.y((this.y() / height).roundToInt())
}

fun Size.normalize(width : Double, height : Double) {
    this.width((this.width() / width).roundToInt())
    this.height((this.height() / height).roundToInt())
}

fun Size2d.normalize(width : Double, height : Double) {
    this.width(this.width() / width)
    this.height(this.height() / height)
}

fun Collection<Point2d>.center() : Point2d {
    var x = 0.0
    var y = 0.0

    this.forEach {
        x += it.x()
        y += it.y()
    }

    x /= this.size
    y /= this.size

    return Point2d(x, y)
}