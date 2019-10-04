package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber



class VideoInputProvider(val videoFilePath : String) : InputProvider {
    var videoGrabber: FrameGrabber = FFmpegFrameGrabber(videoFilePath)

    override fun open() {
        //videoGrabber.format = "mp4"
        videoGrabber.start()
    }

    override fun read(): Frame {
        return videoGrabber.grabFrame()
    }

    override fun close() {
        videoGrabber.stop()
    }

}