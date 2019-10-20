package ch.zhdk.tracking.io

import ch.bildspur.util.TimeKeeper
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber

class CameraInputProvider(deviceNumber : Int = 0,
                          width : Int = 640,
                          height : Int = 480) : InputProvider(width, height) {

    private val grabber = OpenCVFrameGrabber(deviceNumber)

    // todo: implement device selection through list

    override fun open() {
        grabber.imageWidth = width
        grabber.imageHeight = height
        grabber.start()
        super.open()
    }

    override fun read(): Frame {
        val frame = grabber.grab()
        frame.timestamp = TimeKeeper.millis()
        return frame.clone()
    }

    override fun close() {
        grabber.stop()
        super.close()
    }
}