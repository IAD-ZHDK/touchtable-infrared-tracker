package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.RealSense2FrameGrabber

class RealSense2InputProvider(
    val deviceNumber: Int = 0,
    val width: Int = 640,
    val height: Int = 480,
    val frameRate: Int = 30
) : InputProvider {

    private lateinit var rs2: RealSense2FrameGrabber
    private var timestamp = 0L

    override fun open() {
        rs2 = RealSense2FrameGrabber(deviceNumber)
        rs2.enableIRStream(width, height, frameRate)
        rs2.start()
    }

    override fun read(): Frame {
        val frame = rs2.grabIR()
        frame.timestamp = timestamp++
        return frame.clone()
    }

    override fun close() {
        rs2.stop()
        rs2.release()
    }

}