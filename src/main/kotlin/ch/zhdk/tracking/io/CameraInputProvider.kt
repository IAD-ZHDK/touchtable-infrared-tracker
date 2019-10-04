package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber

class CameraInputProvider(val deviceNumber : Int = 0,
                          val width : Int = 640,
                          val height : Int = 480) : InputProvider {

    val grabber = OpenCVFrameGrabber(deviceNumber)

    override fun open() {
        grabber.imageWidth = 640
        grabber.imageHeight = 480
        grabber.start()
    }

    override fun read(): Frame {
        return grabber.grab()
    }

    override fun close() {
        grabber.stop()
    }
}