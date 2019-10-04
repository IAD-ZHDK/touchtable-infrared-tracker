package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.io.InputProvider
import org.bytedeco.opencv.opencv_core.Mat

class SingleTrackingPipeline(inputProvider: InputProvider) : Pipeline(inputProvider) {
    override fun process(frame: Mat): Mat {
        return frame
    }
}