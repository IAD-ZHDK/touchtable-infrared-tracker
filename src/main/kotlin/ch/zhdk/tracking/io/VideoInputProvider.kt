package ch.zhdk.tracking.io

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import java.nio.file.Path
import kotlin.math.roundToLong

class VideoInputProvider(videoFilePath: Path, val frameRate: Double = Double.NaN) : InputProvider {
    private var videoGrabber: FrameGrabber = FFmpegFrameGrabber(videoFilePath.toAbsolutePath().toString())
    private var timestamp = 0L

    override fun open() {
        videoGrabber.start()

        println("video size: ${videoGrabber.imageWidth}x${videoGrabber.imageHeight}")
        println("video framerate: ${videoGrabber.frameRate}")
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

        // using frames as timestamp instead of millis()
        frame.timestamp = timestamp++ //System.currentTimeMillis()

        // return cloned frame
        // (not messing up with start stop of grabber)
        return frame.clone()
    }

    override fun close() {
        videoGrabber.stop()
    }

}