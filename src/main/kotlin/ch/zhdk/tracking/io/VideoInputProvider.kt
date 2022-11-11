package ch.zhdk.tracking.io

import ch.bildspur.util.TimeKeeper
import org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC
import org.bytedeco.ffmpeg.global.avutil.av_log_set_level
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.math.roundToLong

class VideoInputProvider(val videoFilePath: Path, val frameRate: Double = Double.NaN) : InputProvider() {
    private var videoGrabber: FrameGrabber = FFmpegFrameGrabber(videoFilePath.toAbsolutePath().toString())

    override fun open() {
        av_log_set_level(AV_LOG_PANIC)

        if (videoFilePath.toString().isBlank() || !File(videoFilePath.toString()).exists()) {
            throw Exception("Video file path does not exist: '${videoFilePath}'")
        }

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
            videoGrabber.stop()
            videoGrabber.start()
        }

        while(frame == null) {
            frame = videoGrabber.grabFrame()
            Thread.sleep(100)
        }

        frame.timestamp = TimeKeeper.millis()
        return frame
    }

    override fun close() {
        videoGrabber.stop()
        super.close()
    }

}