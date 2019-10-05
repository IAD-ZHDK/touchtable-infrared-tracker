package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber

class CameraInputProvider(val deviceNumber : Int = 0,
                          val width : Int = 640,
                          val height : Int = 480) : InputProvider {

    val grabber = OpenCVFrameGrabber(deviceNumber)

    override fun open() {
        grabber.imageWidth = width
        grabber.imageHeight = height
        grabber.start()
    }

    override fun read(): Frame {
        val frame = grabber.grab()
        frame.timestamp = System.currentTimeMillis()
        return frame.clone()
    }

    override fun close() {
        grabber.stop()
    }
}