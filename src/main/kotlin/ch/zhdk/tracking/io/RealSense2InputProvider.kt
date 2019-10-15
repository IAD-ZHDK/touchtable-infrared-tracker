package ch.zhdk.tracking.io

import ch.zhdk.tracking.javacv.toFrame
import org.bytedeco.ffmpeg.global.avformat.av_register_all
import org.bytedeco.javacpp.Loader
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.javacv.RealSense2FrameGrabber
import org.bytedeco.javacv.RealSenseFrameGrabber

class RealSense2InputProvider(
    val deviceNumber: Int = 0,
    val width: Int = 640,
    val height: Int = 480,
    val frameRate: Int = 30
) : InputProvider {

    private lateinit var grabber: RealSense2FrameGrabber
    private var timestamp = 0L

    override fun open() {
        grabber = RealSense2FrameGrabber(deviceNumber)
        grabber.enableIRStream(width, height, frameRate)
        grabber.start()
    }

    override fun read(): Frame {
        val frame = grabber.grabIR()
        frame.timestamp = timestamp++
        return frame.clone()
    }

    override fun close() {
        grabber.stop()
        grabber.release()
    }

}