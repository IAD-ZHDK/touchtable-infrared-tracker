package ch.zhdk.tracking.io

import ch.bildspur.util.TimeKeeper
import ch.zhdk.tracking.config.InputConfig
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.RealSense2FrameGrabber
import org.bytedeco.librealsense2.global.realsense2.*
import org.bytedeco.opencv.global.opencv_core.IPL_DEPTH_8U
import org.bytedeco.opencv.opencv_core.Size

class RealSense2InputProvider(
    val deviceNumber: Int = 0,
    width: Int = 640,
    height: Int = 480,
    val frameRate: Int = 30,
    val enableRGBIR: Boolean = false,
    val enableDualIR: Boolean = false,
    var displaySecondChannel: Boolean = false,
    var config: InputConfig
) : InputProvider(width, height) {

    private lateinit var rs2: RealSense2FrameGrabber

    override fun open() {
        rs2 = RealSense2FrameGrabber(deviceNumber)
        rs2.enableIRStream(width, height, frameRate)

        if (enableRGBIR) {
            // reset streams
            rs2.disableAllStreams()
            rs2.enableStream(
                RealSense2FrameGrabber.RealSenseStream(
                    RS2_STREAM_INFRARED,
                    0,
                    Size(width, height),
                    frameRate,
                    RS2_FORMAT_BGR8
                )
            )
        }

        if (enableDualIR) {
            println("[RS] enable second stream")
            rs2.enableIRStream(width, height, frameRate, 2)
        }

        // open device
        rs2.open()

        // set settings
        rs2.setSensorOption(RealSense2FrameGrabber.Rs2SensorType.StereoModule, RS2_OPTION_ENABLE_AUTO_EXPOSURE, config.enableAutoExposure.value)

        if(!config.enableAutoExposure.value)
            rs2.setSensorOption(RealSense2FrameGrabber.Rs2SensorType.StereoModule, RS2_OPTION_EXPOSURE, config.autoExposure.value)

        rs2.setSensorOption(
            RealSense2FrameGrabber.Rs2SensorType.StereoModule,
            RS2_OPTION_ENABLE_AUTO_WHITE_BALANCE,
            config.enableAutoWhiteBalance.value
        )

        rs2.setSensorOption(RealSense2FrameGrabber.Rs2SensorType.StereoModule, RS2_OPTION_EMITTER_ENABLED, config.enableIREmitter.value)

        rs2.start()
        super.open()
    }

    override fun read(): Frame {
        rs2.trigger()

        val channel = if (enableDualIR && displaySecondChannel) 1 else 0
        val frame = if (channel == 0 && enableRGBIR)
            rs2.grab(RS2_STREAM_INFRARED, 0, IPL_DEPTH_8U, 3)
        else
            rs2.grabIR(channel)

        return frame
    }

    override fun close() {
        rs2.stop()
        rs2.release()
        super.close()
    }

}