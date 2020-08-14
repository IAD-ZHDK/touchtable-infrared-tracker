package ch.zhdk.tracking.math

import ch.bildspur.math.*
import org.bytedeco.opencv.opencv_core.Point2d

fun Point2d.linearNormalize(width: Double, height: Double): Point2d {
    return Point2d(this.x() / width, this.y() / height)
}

fun Float2.normalize(width : Float, height : Float) : Float2 {
    return Float2(this.x / width, this.y / height)
}

fun Point2d.perspectiveTransform(p1: Float2, p2: Float2, p3: Float2, p4: Float2) : Point2d {
    val result = Float2(this.x().toFloat(), this.y().toFloat()).perspectiveTransform(p1, p2, p3, p4)
    return Point2d(result.x.toDouble(), result.y.toDouble())
}

fun Point2d.perspectiveTransform(transformMat : Mat3) : Point2d {
    val result = Float2(this.x().toFloat(), this.y().toFloat()).perspectiveTransform(transformMat)
    return Point2d(result.x.toDouble(), result.y.toDouble())
}

fun generatePerspectiveTransformInverseMat(p1: Float2, p2: Float2, p3: Float2, p4: Float2) : Mat3 {
    // implementation details: https://math.stackexchange.com/a/3039140
    // First, find the transformation matrix for our deformed rectangle
    // [a b c]
    // [d e f]
    // [g h 1]
    val x0 = p1.x
    val y0 = p1.y
    val x1 = p2.x
    val y1 = p2.y
    val x2 = p3.x
    val y2 = p3.y
    val x3 = p4.x
    val y3 = p4.y
    val dx1 = x1 - x2
    val dx2 = x3 - x2
    val dx3 = x0 - x1 + x2 - x3
    val dy1 = y1 - y2
    val dy2 = y3 - y2
    val dy3 = y0 - y1 + y2 - y3

    val a13 = (dx3 * dy2 - dy3 * dx2) / (dx1 * dy2 - dy1 * dx2)
    val a23 = (dx1 * dy3 - dy1 * dx3) / (dx1 * dy2 - dy1 * dx2)
    val a11 = x1 - x0 + a13 * x1
    val a21 = x3 - x0 + a23 * x3
    val a12 = y1 - y0 + a13 * y1
    val a22 = y3 - y0 + a23 * y3

    val transformMatrix = Mat3(
        Float3(a11, a12, a13),
        Float3(a21, a22, a23),
        Float3(x0, y0, 1f)
    )

    return inverse(transformMatrix)
}

fun Float2.perspectiveTransform(p1: Float2, p2: Float2, p3: Float2, p4: Float2): Float2 {
    val inv = generatePerspectiveTransformInverseMat(p1, p2, p3, p4)
    return this.perspectiveTransform(inv)
}

fun Float2.perspectiveTransform(transformMat : Mat3) : Float2 {
    val v = Float3(this.x, this.y, 1f)
    val result = transformMat * v

    return Float2(result[0] / result[2], result[1] / result[2])
}