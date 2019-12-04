package ch.zhdk.tracking.io

import ch.bildspur.util.TimeKeeper
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import java.nio.file.Path
import kotlin.math.roundToLong

class VideoInputProvider(videoFilePath: Path, val frameRate: Double = Double.NaN) : InputProvider() {
    private var videoGrabber: FrameGrabber = FFmpegFrameGrabber(videoFilePath.toAbsolutePath().toString())

    var frameCount = 0L

    override fun open() {
        videoGrabber.start()

        this.width = videoGrabber.imageWidth
        this.height = videoGrabber.imageHeight

        println("video size: ${videoGrabber.imageWidth}x${videoGrabber.imageHeight}")
        println("video framerate: ${videoGrabber.frameRate}")
        super.open()
    }

    override fun read(): Frame {
        // check if constrain fps
        if(!frameRate.isNaN()) {
            Thread.sleep((1000.0 / frameRate).roundToLong())
        }

        var frame = videoGrabber.grabFrame()

        if(frame == null) {
            println("restarting video")
            videoGrabber.stop()
            videoGrabber.start()
        }

        while(frame == null) {
            frame = videoGrabber.grabFrame()
            Thread.sleep(100)
        }

        frame.timestamp = (frameCount++ * 1000.0 / videoGrabber.frameRate).roundToLong()
        return frame
    }

    override fun close() {
        videoGrabber.stop()
        super.close()
    }

}