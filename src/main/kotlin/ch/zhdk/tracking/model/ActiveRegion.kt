package ch.zhdk.tracking.model

import org.bytedeco.opencv.opencv_core.*

data class ActiveRegion(val center : Point2d,
                        val position : Point,
                        val size : Size,
                        val area : Double,
                        val timestamp : Long,
                        var polygon : Mat = Mat(),
                        var matched : Boolean = false)