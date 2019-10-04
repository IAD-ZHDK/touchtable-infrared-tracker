package ch.zhdk.tracking

import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.CanvasFrame
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder


object JavaCVWebcamRecording {
    @JvmStatic
    fun main(args: Array<String>) {
        OpenCVFrameGrabber.list.forEach { println(it) }

        val grabber = OpenCVFrameGrabber(0)
        println("grabber created")

        grabber.imageWidth = 640
        grabber.imageHeight = 480
        grabber.start()
        println("grabber started")

        var grabbedImage: Frame = grabber.grab()

        val canvasFrame = CanvasFrame("Cam")
        canvasFrame.setCanvasSize(grabbedImage.imageWidth, grabbedImage.imageHeight)

        val recorder = FFmpegFrameRecorder("testvideo.mp4", grabber.imageWidth, grabber.imageHeight)

        recorder.videoCodec = 13
        recorder.format = "mp4"
        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
        recorder.frameRate = 30.0
        //recorder.videoBitrate = 10 * 1024 * 1024

        println("framerate = " + grabber.frameRate)
        grabber.frameRate = grabber.frameRate

        while (canvasFrame.isVisible) {
            grabbedImage = grabber.grab()
            canvasFrame.showImage(grabbedImage)

            if(grabbedImage.imageWidth > 0 )
            recorder.record(grabbedImage)
        }

        recorder.stop();
        grabber.stop()
        canvasFrame.dispose()
    }
}