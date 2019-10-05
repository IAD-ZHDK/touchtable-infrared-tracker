package ch.zhdk.tracking.model

import org.bytedeco.opencv.opencv_core.Point2d

data class ActiveRegion(val position : Point2d,
                        val intensity : Double)