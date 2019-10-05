package ch.zhdk.tracking.javacv

import ch.zhdk.tracking.javacv.analysis.ConnectedComponentsResult
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_core.CV_32S
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*
import kotlin.math.roundToInt

fun Mat.clear() {
    this.setTo(Mat(1, 1, opencv_core.CV_32SC4, Scalar.ALPHA255))
}

fun Mat.zeros(): Mat {
    return this.zeros(this.type())
}

fun Mat.zeros(type: Int): Mat {
    return Mat.zeros(this.rows(), this.cols(), type).asMat()
}

fun Mat.convertColor(color : Int) {
    cvtColor(this, this, color)
}

fun Mat.threshold(thresh: Double, maxval: Double = 255.0, type: Int = THRESH_BINARY) {
    threshold(this, this, thresh, maxval, type)
}

fun Mat.erode(erosionSize: Int) {
    if (erosionSize == 0)
        return

    val element = getStructuringElement(MORPH_RECT,
        Size((2.0 * erosionSize + 1.0).roundToInt(), (2.0 * erosionSize + 1.0).roundToInt()))
    erode(this, this, element)
    element.release()
}

fun Mat.dilate(dilationSize: Int) {
    if (dilationSize == 0)
        return

    val element = getStructuringElement(MORPH_RECT,
        Size((2.0 * dilationSize + 1.0).roundToInt(), (2.0 * dilationSize + 1.0).roundToInt()))
    dilate(this, this, element)
    element.release()
}

fun Mat.connectedComponents(connectivity: Int = 8, ltype: Int = CV_32S): Mat {
    val labeled = this.zeros()
    connectedComponents(this, labeled, connectivity, ltype)
    return labeled
}

fun Mat.connectedComponentsWithStats(connectivity: Int = 8, ltype: Int = CV_32S): ConnectedComponentsResult {
    val labeled = this.zeros()
    val rectComponents = Mat()
    val centComponents = Mat()

    connectedComponentsWithStats(this, labeled, rectComponents, centComponents)
    return ConnectedComponentsResult(labeled, rectComponents, centComponents)
}

fun Mat.imageCenter(): Point2d {
    return Point2d(this.cols() / 2.0, this.rows() / 2.0)
}

fun Point2d.transform(dx: Double, dy: Double): Point2d {
    return Point2d(this.x() + dx, this.y() + dy)
}

fun Point.transform(dx: Int, dy: Int): Point {
    return Point(this.x() + dx, this.y() + dy)
}