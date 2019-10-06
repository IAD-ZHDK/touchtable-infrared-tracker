package ch.zhdk.tracking

import org.bytedeco.javacv.CanvasFrame
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.javacv.VideoInputFrameGrabber


object JavaCVWebcamExample {
    @JvmStatic
    fun main(args: Array<String>) {
        val grabber = OpenCVFrameGrabber(0)
        val desc = VideoInputFrameGrabber.getDeviceDescriptions()
        desc.forEach { println(it) }
        println("grabber created")

        grabber.imageWidth = 640
        grabber.imageHeight = 480
        grabber.start()
        println("grabber started")

        var grabbedImage: Frame = grabber.grab()

        val canvasFrame = CanvasFrame("Cam")
        canvasFrame.setCanvasSize(grabbedImage.imageWidth, grabbedImage.imageHeight)
        canvasFrame.show()

        println("framerate = " + grabber.frameRate)
        grabber.frameRate = grabber.frameRate

        while (canvasFrame.isVisible) {
            grabbedImage = grabber.grab()
            canvasFrame.showImage(grabbedImage)
        }

        grabber.stop()
        canvasFrame.dispose()
    }
}