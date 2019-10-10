package ch.zhdk.tracking

import ch.zhdk.tracking.javacv.toFrame
import org.bytedeco.javacv.*
import org.bytedeco.librealsense.global.RealSense


object JavaCVRealSenseExample {
    @JvmStatic
    fun main(args: Array<String>) {
        val grabber = RealSenseFrameGrabber(0)
        println("grabber created")

        grabber.loadDevice()
        grabber.enableIRStream()
        grabber.imageWidth = 640
        grabber.imageHeight = 480
        grabber.start()
        println("grabber started")

        var grabbedImage: Frame = grabber.grab()

        val canvasFrame = CanvasFrame("RealSense")
        canvasFrame.setCanvasSize(grabbedImage.imageWidth, grabbedImage.imageHeight)

        println("framerate = " + grabber.frameRate)

        while (canvasFrame.isVisible) {
            grabbedImage = grabber.grabIR().toFrame()
            canvasFrame.showImage(grabbedImage)
        }

        grabber.stop()
        canvasFrame.dispose()
    }
}