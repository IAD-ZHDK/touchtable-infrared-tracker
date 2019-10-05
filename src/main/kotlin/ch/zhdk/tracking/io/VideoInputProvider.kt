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
            println("have to restart")
            videoGrabber.frameNumber = 0
            return read()
        }

        return frame
    }

    override fun close() {
        videoGrabber.stop()
    }

}