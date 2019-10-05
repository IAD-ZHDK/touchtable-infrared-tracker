package ch.zhdk.tracking.javacv

import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*
import org.bytedeco.librealsense.global.RealSense.points
import org.bytedeco.opencv.opencv_core.Point2f
import kotlin.math.roundToInt


fun Mat.drawCircle(
    center: Point,
    radius: Int,
    color: Scalar,
    thickness: Int = 1,
    lineTyp: Int = LINE_8,
    shift: Int = 0
) {
    circle(this, center, radius, color, thickness, lineTyp, shift)
}

fun Mat.drawCross(center: Point, size: Int, color: Scalar, thickness: Int = 1, lineTyp: Int = LINE_8, shift: Int = 0) {
    val length = size / 2
    line(
        this,
        Point(center.x() - length, center.y()),
        Point(center.x() + length, center.y()),
        color,
        thickness,
        lineTyp,
        shift
    )
    line(
        this,
        Point(center.x(), center.y() - length),
        Point(center.x(), center.y() + length),
        color,
        thickness,
        lineTyp,
        shift
    )
}

fun Mat.drawText(
    text: String,
    position: Point,
    color: Scalar,
    thickness: Int = 1,
    fontFace: Int = FONT_HERSHEY_SCRIPT_SIMPLEX,
    scale: Double = 1.0,
    lineTyp: Int = LINE_8,
    bottomLeftOrigin: Boolean = false
) {
    putText(this, text, position, fontFace, scale, color, thickness, lineTyp, bottomLeftOrigin)
}

fun Mat.drawRect(rect: Rect, color: Scalar, thickness: Int = 1) {
    this.drawRect(rect.tl(), rect.size(), color, thickness)
}

fun Mat.drawRect(origin: Point, size: Size, color: Scalar, thickness: Int = 1, lineTyp: Int = LINE_8, shift: Int = 0) {
    rectangle(this, origin, origin.transform(size.width(), size.height()), color, thickness, lineTyp, shift)
}

fun Mat.drawLine(pt1: Point, pt2: Point, color: Scalar, thickness: Int = 1, lineTyp: Int = LINE_8, shift: Int = 0) {
    line(this, pt1, pt2, color, thickness, lineTyp, shift)
}

fun Mat.drawMarker(
    position: Point,
    color: Scalar,
    markerType: Int = MARKER_CROSS,
    markerSize: Int = 20,
    thickness: Int = 1,
    lineType: Int = LINE_8
) {
    drawMarker(this, position, color, markerType, markerSize, thickness, lineType)
}

fun Mat.drawRotatedRect(rect: RotatedRect, color: Scalar, thickness: Int = 1, lineTyp: Int = LINE_8, shift: Int = 0) {
    val vertices = Point2f(4)
    rect.points(vertices)
    val coordinates = FloatArray(8)
    vertices.get(coordinates)

    for (i in 0 until 8 step 2) {
        val p1 = Point(coordinates[i].roundToInt(), coordinates[i + 1].roundToInt())
        val p2 = Point(
            coordinates[(i + 2) % coordinates.size].roundToInt(),
            coordinates[(i + 2 + 1) % coordinates.size].roundToInt()
        )

        this.drawLine(p1, p2, color, thickness, lineTyp, shift)
    }
}