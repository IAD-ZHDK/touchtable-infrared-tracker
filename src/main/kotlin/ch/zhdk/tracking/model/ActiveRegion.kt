package ch.zhdk.tracking.model

import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Point2d
import org.bytedeco.opencv.opencv_core.Size

data class ActiveRegion(
    val center: Point2d,
    val position: Point,
    val size: Size,
    val area: Double,
    val timestamp: Long,

    // match related
    var matched: Boolean = false
)