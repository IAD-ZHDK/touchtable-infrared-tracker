package ch.zhdk.tracking.io

import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.RealSense2FrameGrabber

class RealSense2InputProvider(
    val deviceNumber: Int = 0,
    width: Int = 640,
    height: Int = 480,
    val frameRate: Int = 30,
    val enableDualIR : Boolean = false,
    var displaySecondChannel : Boolean = false
) : InputProvider(width, height) {

    private lateinit var rs2: RealSense2FrameGrabber
    private var timestamp = 0L

    override fun open() {
        rs2 = RealSense2FrameGrabber(deviceNumber)
        rs2.enableIRStream(width, height, frameRate)

        if(enableDualIR) {
            println("[RS] enable second stream")
            rs2.enableIRStream(width, height, frameRate, 2)
        }

        rs2.disableIREmitter()
        rs2.start()
        super.open()
    }

    override fun read(): Frame {
        rs2.trigger()

        val channel = if(enableDualIR && displaySecondChannel) 1 else 0
        val frame = rs2.grabIR(channel)

        frame.timestamp = timestamp++
        return frame.clone()
    }

    override fun close() {
        rs2.stop()
        rs2.release()
        super.close()
    }

}