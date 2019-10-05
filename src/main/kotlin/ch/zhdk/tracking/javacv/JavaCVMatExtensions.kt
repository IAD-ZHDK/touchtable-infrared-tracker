package ch.zhdk.tracking.javacv

import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import kotlin.math.roundToInt

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