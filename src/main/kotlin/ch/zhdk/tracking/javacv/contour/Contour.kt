package ch.zhdk.tracking.javacv.contour

import ch.zhdk.tracking.javacv.approxPolyDP
import ch.zhdk.tracking.javacv.toMatOfPoint2f
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*

class Contour(val nativeContour: Mat) {

    private val contour2F: Mat by lazy { nativeContour.toMatOfPoint2f() }

    fun moments(): Moments {
        return moments(nativeContour)
    }

    fun area(oriented: Boolean = false): Double {
        return contourArea(nativeContour, oriented)
    }

    fun arcLength(closed: Boolean = false): Double {
        return arcLength(nativeContour, closed)
    }

    fun approxPolyDP(epsilon: Double): Mat {
        return nativeContour.approxPolyDP(epsilon)
    }

    fun isContourConvex(): Boolean {
        return isContourConvex(nativeContour)
    }

    fun boundingBox(): Rect {
        return boundingRect(nativeContour)
    }

    fun minAreaBox(): RotatedRect {
        val box = RotatedRect()
        boxPoints(box, nativeContour)
        return box
    }

    fun drawContour(dest: Mat, color: Scalar) {
        drawContours(dest, MatVector(nativeContour), -1, color)
    }

    fun release() {
        nativeContour.release()
        contour2F.release()
    }
}