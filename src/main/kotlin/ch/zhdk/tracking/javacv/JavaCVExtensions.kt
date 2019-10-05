package ch.zhdk.tracking.javacv

import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat

fun Mat.convertColor(color : Int) {
    cvtColor(this, this, color)
}

fun Mat.threshold(thresh: Double, maxval: Double = 255.0, type: Int = THRESH_BINARY) {
    threshold(this, this, thresh, maxval, type)
}