package ch.zhdk.tracking

import org.bytedeco.javacv.CanvasFrame
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.nio.file.Paths


object JavaCVVideoExample {
    @JvmStatic
    fun main(args: Array<String>) {
        OpenCVFrameGrabber.list.forEach { println(it) }

        val grabber = FFmpegFrameGrabber(Paths.get("data/irMovieSample.mov").toAbsolutePath().toString())
        println("ffmpeg grabber created")

        grabber.imageWidth = 1280
        grabber.imageHeight = 720
        grabber.start()
        println("grabber started")

        var grabbedImage: Frame? = grabber.grab()

        val canvasFrame = CanvasFrame("Video")
        canvasFrame.setCanvasSize(1280, 720)

        println("framerate = " + grabber.frameRate)
        grabber.frameRate = grabber.frameRate

        while (canvasFrame.isVisible) {
            grabbedImage = grabber.grab()
            println(grabber.frameNumber)

            if(grabbedImage == null) {
                grabber.frameNumber = 0
                grabbedImage = grabber.grab()
            }
            canvasFrame.showImage(grabbedImage)
        }

        grabber.stop()
        canvasFrame.dispose()
    }
}