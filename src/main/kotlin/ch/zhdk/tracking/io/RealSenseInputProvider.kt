package ch.zhdk.tracking.io

import ch.zhdk.tracking.javacv.toFrame
import org.bytedeco.ffmpeg.global.avformat.av_register_all
import org.bytedeco.javacpp.Loader
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.javacv.RealSenseFrameGrabber

class RealSenseInputProvider(val deviceNumber : Int = 0,
    val width : Int = 640,
    val height : Int = 480) : InputProvider {

    private lateinit var grabber : RealSenseFrameGrabber
    private var timestamp = 0L

    // todo: implement device selection through list

    override fun open() {
        RealSenseFrameGrabber.tryLoad()

        grabber = RealSenseFrameGrabber(deviceNumber)
        grabber.enableIRStream()
        grabber.irImageWidth = width
        grabber.irImageHeight = height
        grabber.start()
    }

    override fun read(): Frame {
        val frame = grabber.grabIR().toFrame()
        frame.timestamp = timestamp++
        return frame.clone()
    }

    override fun close() {
        grabber.stop()
    }

}