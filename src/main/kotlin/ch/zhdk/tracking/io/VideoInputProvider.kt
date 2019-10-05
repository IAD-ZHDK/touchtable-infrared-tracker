package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import java.nio.file.Path
import kotlin.math.roundToLong


class VideoInputProvider(val videoFilePath : Path, val useVideoFrameRate: Boolean = true) : InputProvider {
    var videoGrabber: FrameGrabber = FFmpegFrameGrabber(videoFilePath.toAbsolutePath().toString())

    override fun open() {
        videoGrabber.format = "mov"
        videoGrabber.start()

        println(videoGrabber)
    }

    override fun read(): Frame {
        if(useVideoFrameRate) {
            Thread.sleep(videoGrabber.frameRate.roundToLong())
        }

        val frame = videoGrabber.grabFrame()

        if(frame == null) {
            println("restarting video")
            videoGrabber.stop()
            videoGrabber.frameNumber = 0
            videoGrabber.start()
            return read()
        }

        // return cloned frame
        // (not messing up with start stop of grabber)
        return frame.clone()
    }

    override fun close() {
        videoGrabber.stop()
    }

}